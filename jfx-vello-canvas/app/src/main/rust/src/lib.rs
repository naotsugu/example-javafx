use std::borrow::Cow;
use std::slice;
use std::ffi::{c_char, CStr};
use vello::{
    kurbo::{Affine, BezPath, Cap, Rect, Stroke, Ellipse, RoundedRect, Line},
    peniko::{Color, Fill}, Scene, Glyph};
use vello::wgpu;
use parley::{Alignment, AlignmentOptions, FontContext, FontFamily, Layout,
             LayoutContext, PositionedLayoutItem, StyleProperty};

pub struct RenderContext {
    device: wgpu::Device,
    queue: wgpu::Queue,
    renderer: vello::Renderer,
    texture: wgpu::Texture,
    readback_buffer: wgpu::Buffer,
    width: u32,
    height: u32,
    scene: Scene,
    fill_color: Color,
    stroke_color: Color,
    line_width: f64,
    line_cap: Cap,
    font_cx: FontContext,
    layout_cx: LayoutContext<()>,
    font_family: String,
    font_size: f32,
}

impl RenderContext {
    /// Builds the Stroke from the current line width and cap.
    fn stroke_style(&self) -> Stroke {
        Stroke::new(self.line_width).with_caps(self.line_cap)
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn create_render_context(width: u32, height: u32) -> *mut RenderContext {

    // initialize wgpu device and queue for GPU rendering
    let instance = wgpu::Instance::default();
    let adapter = pollster::block_on(instance.request_adapter(&wgpu::RequestAdapterOptions {
        power_preference: wgpu::PowerPreference::default(),
        force_fallback_adapter: false,
        compatible_surface: None,
        ..Default::default()
    }))
    .expect("failed to find an appropriate adapter");
    let (device, queue) = pollster::block_on(adapter.request_device(&wgpu::DeviceDescriptor {
        label: Some("Device"),
        required_features: wgpu::Features::empty(),
        ..Default::default()
    }))
    .expect("failed to create device");

    // create a render target texture and a readback buffer
    let (texture, readback_buffer) = create_render_target(&device, width, height);

    // create renderer and render the scene to the texture
    let renderer = vello::Renderer::new(
        &device,
        vello::RendererOptions {
            use_cpu: false,
            antialiasing_support: vello::AaSupport::all(),
            num_init_threads: None,
            pipeline_cache: None,
        },
    ).expect("failed to create renderer");

    let scene = Scene::new();

    let ctx = RenderContext {
        device,
        queue,
        renderer,
        texture,
        readback_buffer,
        width,
        height,
        scene,
        fill_color: Color::from_rgba8(0, 0, 0, 255),
        stroke_color: Color::from_rgba8(0, 0, 0, 255),
        line_width: 1.,
        line_cap: Cap::Butt,
        font_cx: FontContext::new(),
        layout_cx: LayoutContext::new(),
        font_family: "sans-serif".to_string(),
        font_size: 14.0,
    };

    // detach from Rust's memory management and pass to Java as a raw pointer
    Box::into_raw(Box::new(ctx))
}

#[unsafe(no_mangle)]
pub extern "C" fn destroy_render_context(ctx_ptr: *mut RenderContext) {
    if !ctx_ptr.is_null() {
        // reconstruct the Box from the raw pointer to trigger
        // automatic dropping (memory deallocation) when going out of scope
        unsafe { let _ = Box::from_raw(ctx_ptr); }
        println!("RenderContext destroyed.");
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn render(ctx_ptr: *mut RenderContext, buffer: *mut u8) {

    if ctx_ptr.is_null() || buffer.is_null() {
        return;
    }

    // restore the reference from the raw pointer (without taking ownership)
    let ctx = unsafe { &mut *ctx_ptr };
    let (device, queue, renderer, texture, readback_buffer, width, height, scene) = (
        &ctx.device,
        &ctx.queue,
        &mut ctx.renderer,
        &ctx.texture,
        &mut ctx.readback_buffer,
        ctx.width,
        ctx.height,
        &mut ctx.scene
    );

    // drow to the GPU
    renderer.render_to_texture(
        &device,
        queue,
        &scene,
        &texture.create_view(&wgpu::TextureViewDescriptor::default()),
        &vello::RenderParams {
            base_color: Color::TRANSPARENT,
            width, height,
            antialiasing_method: vello::AaConfig::Msaa16,
        },
    ).expect("failed to render texture");

    let padded_row = padded_bytes_per_row(width);
    let mut encoder = device.create_command_encoder(
        &wgpu::CommandEncoderDescriptor::default());
    encoder.copy_texture_to_buffer(
        wgpu::TexelCopyTextureInfo {
            texture,
            mip_level: 0,
            origin: wgpu::Origin3d::ZERO,
            aspect: wgpu::TextureAspect::All,
        },
        wgpu::TexelCopyBufferInfo {
            buffer: readback_buffer,
            layout: wgpu::TexelCopyBufferLayout {
                offset: 0,
                bytes_per_row: Some(padded_row), // was width * 4
                rows_per_image: Some(height),
            },
        },
        wgpu::Extent3d {
            width, height, depth_or_array_layers: 1
        },
    );
    queue.submit(Some(encoder.finish()));

    let buffer_slice = readback_buffer.slice(..);
    let (tx, rx) = std::sync::mpsc::channel();
    buffer_slice.map_async(wgpu::MapMode::Read, move |v| tx.send(v).unwrap());

    device.poll(wgpu::PollType::wait_indefinitely()).unwrap();
    rx.recv().unwrap().unwrap();
    let mapped_data = buffer_slice.get_mapped_range().unwrap();

    // // SIMD copy
    // let length = (width * height * 4) as usize;
    // let src_bytes = unsafe { slice::from_raw_parts(mapped_data.as_ptr(), length) };
    // let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, length) };
    // for (src, dst) in src_bytes.chunks_exact(4).zip(dst_bytes.chunks_exact_mut(4)) {
    //     dst[0] = src[2]; // B <- R
    //     dst[1] = src[1]; // G <- G
    //     dst[2] = src[0]; // R <- B
    //     dst[3] = src[3]; // A <- A
    // }

    // copy row by row: drop the row padding and convert RGBA to BGRA
    let row_bytes = (width * 4) as usize;
    let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, row_bytes * height as usize) };
    for (src_row, dst_row) in mapped_data
        .chunks_exact(padded_row as usize)
        .zip(dst_bytes.chunks_exact_mut(row_bytes))
    {
        for (src, dst) in src_row[..row_bytes].chunks_exact(4).zip(dst_row.chunks_exact_mut(4)) {
            dst[0] = src[2]; // B <- R
            dst[1] = src[1]; // G <- G
            dst[2] = src[0]; // R <- B
            dst[3] = src[3]; // A <- A
        }
    }

    drop(mapped_data);
    readback_buffer.unmap();
    scene.reset();
}

/// Resizes the canvas. The size must be non-zero and within the device limits.
/// Returns false if the size is invalid; the old size is kept in that case.
/// Call this before drawing a frame: commands already added to the scene are kept as they are.
/// After resizing, the buffer passed to render() must hold width * height * 4 bytes.
#[unsafe(no_mangle)]
pub extern "C" fn resize(ctx_ptr: *mut RenderContext, width: u32, height: u32) -> bool {
    if ctx_ptr.is_null() || width == 0 || height == 0 { return false; }
    let ctx = unsafe { &mut *ctx_ptr };
    if width == ctx.width && height == ctx.height { return true; }

    let max = ctx.device.limits().max_texture_dimension_2d;
    if width > max || height > max { return false; }

    // the old texture and buffer are dropped when replaced
    let (texture, readback_buffer) = create_render_target(&ctx.device, width, height);
    ctx.texture = texture;
    ctx.readback_buffer = readback_buffer;
    ctx.width = width;
    ctx.height = height;
    true
}

// -- attribute ---------------------------------------------------------------

/// Sets the current fill paint attribute. The default value is BLACK.
#[unsafe(no_mangle)]
pub extern "C" fn set_fill(ctx_ptr: *mut RenderContext, r: u8, g: u8, b: u8, a: u8) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.fill_color = Color::from_rgba8(r, g, b, a);
}

/// Sets the current stroke paint attribute. The default value is BLACK.
#[unsafe(no_mangle)]
pub extern "C" fn set_stroke(ctx_ptr: *mut RenderContext, r: u8, g: u8, b: u8, a: u8) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.stroke_color = Color::from_rgba8(r, g, b, a);
}

/// Sets the width; it applies to all following stroke operations
#[unsafe(no_mangle)]
pub extern "C" fn set_line_width(ctx_ptr: *mut RenderContext, line_width: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.line_width = line_width;
}

/// Sets the line cap: 0 = butt, 1 = round, 2 = square (same values as Java's BasicStroke).
/// Unknown values are ignored.
#[unsafe(no_mangle)]
pub extern "C" fn set_line_cap(ctx_ptr: *mut RenderContext, cap: u32) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.line_cap = match cap {
        0 => Cap::Butt,
        1 => Cap::Round,
        2 => Cap::Square,
        _ => return,
    };
}

/// Sets the font families as a comma-separated list, e.g. "Segoe UI, Meiryo, sans-serif".
/// Characters missing in a family fall back to the next one, then to other system fonts.
/// Returns false if none of the listed (non-generic) families is installed;
/// the list is stored anyway.
#[unsafe(no_mangle)]
pub extern "C" fn set_font_family(ctx_ptr: *mut RenderContext, names: *const u8, len: u32) -> bool {
    if ctx_ptr.is_null() || names.is_null() { return false; }
    let ctx = unsafe { &mut *ctx_ptr };
    let bytes = unsafe { slice::from_raw_parts(names, len as usize) };
    let Ok(names) = std::str::from_utf8(bytes) else { return false; };
    ctx.font_family = names.trim().to_string();

    // generic names (serif, sans-serif, ...) are resolved by the system and always count as found
    names.split(',')
        .map(|n| n.trim().trim_matches(|c| c == '"' || c == '\''))
        .filter(|n| !n.is_empty())
        .any(|n| {
            matches!(
                n.to_ascii_lowercase().as_str(),
                "serif" | "sans-serif" | "monospace" | "cursive" | "fantasy" | "system-ui"
            ) || ctx.font_cx.collection.family_id(n).is_some()
        })
}

/// Sets the font size in pixels. Non-positive or non-finite values are ignored.
#[unsafe(no_mangle)]
pub extern "C" fn set_font_size(ctx_ptr: *mut RenderContext, size: f64) {
    if ctx_ptr.is_null() || !size.is_finite() || size <= 0.0 { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.font_size = size as f32;
}

// -- draw --------------------------------------------------------------------

/// Fills a rectangle using the current fill paint.
#[unsafe(no_mangle)]
pub extern "C" fn fill_rect(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let (scene, fill_color) = (&mut ctx.scene, ctx.fill_color);
    scene.fill(
        Fill::NonZero,
        Affine::IDENTITY,
        fill_color,
        None,
        &Rect::new(x, y, x + width, y + height),
    );
}

#[unsafe(no_mangle)]
pub extern "C" fn stroke_rect(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let stroke = ctx.stroke_style();
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &Rect::new(x, y, x + width, y + height),
    );
}

/// Fills an oval inscribed in the given bounding rectangle using the current fill paint.
#[unsafe(no_mangle)]
pub extern "C" fn fill_oval(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let (scene, fill_color) = (&mut ctx.scene, ctx.fill_color);
    // center is the middle of the bounding box, radii are half of its size
    let oval = Ellipse::new((x + width / 2.0, y + height / 2.0), (width / 2.0, height / 2.0), 0.0);
    scene.fill(
        Fill::NonZero,
        Affine::IDENTITY,
        fill_color,
        None,
        &oval,
    );
}

/// Strokes an oval inscribed in the given bounding rectangle using the current stroke paint and width.
#[unsafe(no_mangle)]
pub extern "C" fn stroke_oval(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let stroke = ctx.stroke_style();
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    // center is the middle of the bounding box, radii are half of its size
    let oval = Ellipse::new((x + width / 2.0, y + height / 2.0), (width / 2.0, height / 2.0), 0.0);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &oval,
    );
}

/// Fills a rounded rectangle using the current fill paint.
/// radius is the radius of the corner arcs.
#[unsafe(no_mangle)]
pub extern "C" fn fill_round_rect(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64, radius: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let (scene, fill_color) = (&mut ctx.scene, ctx.fill_color);
    scene.fill(
        Fill::NonZero,
        Affine::IDENTITY,
        fill_color,
        None,
        &RoundedRect::new(x, y, x + width, y + height, radius),
    );
}

/// Strokes a rounded rectangle using the current stroke paint and width.
/// radius is the radius of the corner arcs.
#[unsafe(no_mangle)]
pub extern "C" fn stroke_round_rect(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64, radius: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let stroke = ctx.stroke_style();
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &RoundedRect::new(x, y, x + width, y + height, radius),
    );
}

/// Strokes a line from (x1, y1) to (x2, y2) using the current stroke paint and width.
#[unsafe(no_mangle)]
pub extern "C" fn stroke_line(ctx_ptr: *mut RenderContext,
        x1: f64, y1: f64, x2: f64, y2: f64) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let stroke = ctx.stroke_style();
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &Line::new((x1, y1), (x2, y2)),
    );
}


/// Fills a closed polygon using the current fill paint.
/// The even-odd rule is used, same as Java's fillPolygon.
#[unsafe(no_mangle)]
pub extern "C" fn fill_polygon(ctx_ptr: *mut RenderContext,
        x_points: *const f64, y_points: *const f64, n_points: u32) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let Some(path) = (unsafe { points_to_path(x_points, y_points, n_points, true) }) else { return; };
    let (scene, fill_color) = (&mut ctx.scene, ctx.fill_color);
    scene.fill(
        Fill::EvenOdd,
        Affine::IDENTITY,
        fill_color,
        None,
        &path,
    );
}

/// Strokes a closed polygon using the current stroke paint, width and cap.
#[unsafe(no_mangle)]
pub extern "C" fn stroke_polygon(ctx_ptr: *mut RenderContext,
        x_points: *const f64, y_points: *const f64, n_points: u32) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let Some(path) = (unsafe { points_to_path(x_points, y_points, n_points, true) }) else { return; };
    let stroke = ctx.stroke_style(); // build before borrowing scene mutably
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &path,
    );
}

/// Strokes an open polyline (the last point is not connected to the first one)
/// using the current stroke paint, width and cap.
#[unsafe(no_mangle)]
pub extern "C" fn stroke_polyline(ctx_ptr: *mut RenderContext,
        x_points: *const f64, y_points: *const f64, n_points: u32) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let Some(path) = (unsafe { points_to_path(x_points, y_points, n_points, false) }) else { return; };
    let stroke = ctx.stroke_style(); // build before borrowing scene mutably
    let (scene, stroke_color) = (&mut ctx.scene, ctx.stroke_color);
    scene.stroke(
        &stroke,
        Affine::IDENTITY,
        stroke_color,
        None,
        &path,
    );
}

/// Draws UTF-8 text.
/// (x, y) is the left end of the baseline of the first line.
#[unsafe(no_mangle)]
pub extern "C" fn fill_text(ctx_ptr: *mut RenderContext,
        text_ptr: *const c_char, x: f64, y: f64) {
    if ctx_ptr.is_null() || text_ptr.is_null()  { return; }
    let c_str = unsafe { CStr::from_ptr(text_ptr) };
    let text = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return, // illegal UTF-8
    };
    fill_text_internal(ctx_ptr, text, x, y);
}

/// Draws UTF-8 text.
/// (x, y) is the left end of the baseline of the first line.
#[unsafe(no_mangle)]
pub extern "C" fn fill_seg_text(ctx_ptr: *mut RenderContext,
        text: *const u8, len: u32, x: f64, y: f64) {
    if ctx_ptr.is_null() || text.is_null() { return; }
    let bytes = unsafe { slice::from_raw_parts(text, len as usize) };
    let Ok(text) = std::str::from_utf8(bytes) else { return; };
    fill_text_internal(ctx_ptr, text, x, y);
}

// -- private -----------------------------------------------------------------

/// Builds a path from separate x / y coordinate arrays.
/// Returns None if a pointer is null or there are fewer than 2 points.
/// The caller must guarantee that both pointers are valid for n_points elements.
unsafe fn points_to_path(
    x_points: *const f64, y_points: *const f64, n_points: u32, close: bool) -> Option<BezPath> {
    if x_points.is_null() || y_points.is_null() || n_points < 2 {
        return None;
    }
    let n = n_points as usize;
    let xs = unsafe { slice::from_raw_parts(x_points, n) };
    let ys = unsafe { slice::from_raw_parts(y_points, n) };

    let mut path = BezPath::new();
    path.move_to((xs[0], ys[0]));
    for i in 1..n {
        path.line_to((xs[i], ys[i]));
    }
    if close {
        // connect the last point back to the first one
        path.close_path();
    }
    Some(path)
}

fn fill_text_internal(ctx_ptr: *mut RenderContext, text: &str, x: f64, y: f64) {

    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };

    let size = ctx.font_size;
    let fill_color = ctx.fill_color;

    let mut builder = ctx.layout_cx.ranged_builder(&mut ctx.font_cx, text, 1.0, true);
    builder.push_default(StyleProperty::FontSize(size));
    builder.push_default(StyleProperty::FontFamily(FontFamily::Source(
        Cow::Borrowed(ctx.font_family.as_str()),
    )));

    let mut layout: Layout<()> = builder.build(text);
    layout.break_all_lines(None); // no wrapping
    layout.align(Alignment::Start, AlignmentOptions::default());

    // place the baseline of the first line at y
    let Some(first_line) = layout.lines().next() else { return; };
    let origin_y = y as f32 - first_line.metrics().baseline;

    let scene = &mut ctx.scene;
    for line in layout.lines() {
        for item in line.items() {
            let PositionedLayoutItem::GlyphRun(glyph_run) = item else { continue; };
            let run = glyph_run.run();
            let baseline = origin_y + glyph_run.baseline();
            let mut cursor = x as f32 + glyph_run.offset();
            scene
                .draw_glyphs(run.font())
                .font_size(run.font_size())
                .normalized_coords(run.normalized_coords())
                .brush(fill_color)
                .draw(Fill::NonZero, glyph_run.glyphs().map(|g| {
                    // g.x / g.y are offsets inside the run; y points up in parley
                    let glyph = Glyph { id: g.id, x: cursor + g.x, y: baseline - g.y };
                    cursor += g.advance;
                    glyph
                }));
        }
    }
}

/// wgpu requires bytes_per_row of a texture-to-buffer copy to be a multiple of 256.
fn padded_bytes_per_row(width: u32) -> u32 {
    (width * 4).next_multiple_of(wgpu::COPY_BYTES_PER_ROW_ALIGNMENT)
}

/// Creates the render target texture and the readback buffer for the given size.
fn create_render_target(device: &wgpu::Device, width: u32, height: u32)
        -> (wgpu::Texture, wgpu::Buffer) {
    let texture = device.create_texture(&wgpu::TextureDescriptor {
        label: Some("Render Target Texture"),
        size: wgpu::Extent3d {
            width,
            height,
            depth_or_array_layers: 1,
        },
        mip_level_count: 1,
        sample_count: 1,
        dimension: wgpu::TextureDimension::D2,
        format: wgpu::TextureFormat::Rgba8Unorm,
        usage: wgpu::TextureUsages::RENDER_ATTACHMENT
            | wgpu::TextureUsages::COPY_SRC
            | wgpu::TextureUsages::STORAGE_BINDING,
        view_formats: &[],
    });

    let readback_buffer = device.create_buffer(&wgpu::BufferDescriptor {
        label: Some("Readback Buffer"),
        // each row is padded, so the buffer is larger than width * height * 4
        size: (padded_bytes_per_row(width) * height) as wgpu::BufferAddress,
        usage: wgpu::BufferUsages::MAP_READ | wgpu::BufferUsages::COPY_DST,
        mapped_at_creation: false,
    });

    (texture, readback_buffer)
}
