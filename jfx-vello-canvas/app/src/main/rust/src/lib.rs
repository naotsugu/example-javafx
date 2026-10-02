use std::slice;
use std::sync::Arc;
use vello::{
    kurbo::{Affine, BezPath, Cap, Rect, Stroke, Ellipse, RoundedRect, Line},
    peniko::{Color, Fill}, Scene, Glyph};
use vello::peniko::{Blob, FontData};
use vello::wgpu;
use skrifa::{FontRef, MetadataProvider};
use skrifa::instance::{LocationRef, Size};

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
    font: Option<FontData>,
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

    // create a render target texture
    let texture = device.create_texture(&wgpu::TextureDescriptor {
        label: Some("Render Target Texture"),
        size: wgpu::Extent3d {
            width: width.into(),
            height: height.into(),
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
        size: (width * height * 4) as wgpu::BufferAddress,
        usage: wgpu::BufferUsages::MAP_READ | wgpu::BufferUsages::COPY_DST,
        mapped_at_creation: false,
    });

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
        font: None,
        font_size: 12.0,
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
                bytes_per_row: Some(width * 4),
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
    let mapped_data = buffer_slice.get_mapped_range();

    // SIMD copy
    let length = (width * height * 4) as usize;
    let src_bytes = unsafe { slice::from_raw_parts(mapped_data.as_ptr(), length) };
    let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, length) };
    for (src, dst) in src_bytes.chunks_exact(4).zip(dst_bytes.chunks_exact_mut(4)) {
        dst[0] = src[2]; // B <- R
        dst[1] = src[1]; // G <- G
        dst[2] = src[0]; // R <- B
        dst[3] = src[3]; // A <- A
    }

    drop(mapped_data);
    readback_buffer.unmap();
    scene.reset();
}

// --

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

/// Sets the current font from raw font file data (TTF / OTF). The data is copied.
/// index selects a font inside a collection (use 0 for a single font file).
/// Returns false if the data cannot be parsed; the previous font is kept in that case.
#[unsafe(no_mangle)]
pub extern "C" fn set_font(ctx_ptr: *mut RenderContext,
                           data: *const u8, len: u32, index: u32) -> bool {
    if ctx_ptr.is_null() || data.is_null() { return false; }
    let ctx = unsafe { &mut *ctx_ptr };
    let bytes = unsafe { slice::from_raw_parts(data, len as usize) }.to_vec();
    // validate before storing so that fill_text never sees a broken font
    if FontRef::from_index(&bytes, index).is_err() { return false; }
    ctx.font = Some(FontData::new(Blob::new(Arc::new(bytes)), index));
    true
}

/// Sets the font size in pixels. Non-positive or non-finite values are ignored.
#[unsafe(no_mangle)]
pub extern "C" fn set_font_size(ctx_ptr: *mut RenderContext, size: f64) {
    if ctx_ptr.is_null() || !size.is_finite() || size <= 0.0 { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    ctx.font_size = size as f32;
}

// --------------------------------------------------------------

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

/// Draws UTF-8 text using the current fill paint, font and font size.
/// (x, y) is the left end of the baseline, same as Java's drawString.
/// Nothing is drawn if no font has been set.
#[unsafe(no_mangle)]
pub extern "C" fn fill_text(ctx_ptr: *mut RenderContext,
        text: *const u8, len: u32, x: f64, y: f64) {
    if ctx_ptr.is_null() || text.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let Some(font) = ctx.font.clone() else { return; };
    let bytes = unsafe { slice::from_raw_parts(text, len as usize) };
    let Ok(text) = std::str::from_utf8(bytes) else { return; };
    let Ok(font_ref) = FontRef::from_index(font.data.data(), font.index) else { return; };

    // simple layout: map each char to a glyph and advance by its width
    // (no kerning, ligatures or complex script shaping)
    let size = ctx.font_size;
    let charmap = font_ref.charmap();
    let metrics = font_ref.glyph_metrics(Size::new(size), LocationRef::default());
    let mut pen_x = x as f32;
    let glyphs: Vec<Glyph> = text.chars().map(|c| {
        let gid = charmap.map(c).unwrap_or_default();
        let glyph = Glyph { id: gid.to_u32(), x: pen_x, y: y as f32 };
        pen_x += metrics.advance_width(gid).unwrap_or_default();
        glyph
    }).collect();

    let (scene, fill_color) = (&mut ctx.scene, ctx.fill_color);
    scene
        .draw_glyphs(&font)
        .font_size(size)
        .brush(fill_color)
        .draw(Fill::NonZero, glyphs.into_iter());
}
