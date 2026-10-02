use std::slice;
use std::sync::Arc;
use vello::{
    kurbo::{Affine, Rect},
    peniko::{Blob, Color, Fill},
    Glyph, Scene,
};
use skrifa::{FontRef, MetadataProvider};
use vello::peniko::{Brush, FontData};
use std::ffi::{c_char, CStr};
use std::io::BufWriter;
use vello::wgpu;


pub struct RenderContext {
    device: wgpu::Device,
    queue: wgpu::Queue,
    renderer: vello::Renderer,
    texture: wgpu::Texture,
    readback_buffer: wgpu::Buffer,
    width: u32,
    height: u32,
}

#[unsafe(no_mangle)]
pub extern "C" fn create_render_context(width: u32, height: u32) -> *mut RenderContext {

    // Initialize wgpu device and queue for GPU rendering
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

    // Create a render target texture
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

    let renderer = vello::Renderer::new(
        &device,
        vello::RendererOptions {
            use_cpu: false,
            antialiasing_support: vello::AaSupport::all(),
            num_init_threads: None,
            pipeline_cache: None,
        },
    ).expect("failed to create renderer");

    let ctx = RenderContext {
        device,
        queue,
        renderer,
        texture,
        readback_buffer,
        width,
        height,
    };

    // detach from Rust's memory management and pass to Java as a raw pointer
    Box::into_raw(Box::new(ctx))
}

// #[unsafe(no_mangle)]
// pub extern "C" fn add(a: i32, b: i32) -> i32 {
//     a + b
// }
//
// use std::slice;
// use std::sync::Arc;
// use font_kit::family_name::FamilyName;
// use font_kit::properties::Properties;
// use font_kit::source::SystemSource;
// use vello_cpu::{RenderContext, Resources, Pixmap, Glyph};
// use vello_cpu::{color::{palette::css}, kurbo::Rect};
// use vello_cpu::peniko::{Blob, FontData};
// use skrifa::{FontRef, MetadataProvider};
//
// pub struct Context {
//     render_context: RenderContext,
//     resources: Resources,
//     pixmap: Pixmap,
//     width: u32,
//     height: u32,
// }
//
// #[unsafe(no_mangle)]
// pub extern "C" fn create_ctx(width: u32, height: u32) -> *mut Context {
//     let ctx = Context {
//         render_context: RenderContext::new(width as u16, height as u16),
//         resources: Resources::new(),
//         pixmap: Pixmap::new(width as u16, height as u16),
//         width, height
//     };
//     Box::into_raw(Box::new(ctx))
// }
//
// #[unsafe(no_mangle)]
// pub extern "C" fn destroy_ctx(ctx_ptr: *mut Context) {
//     if !ctx_ptr.is_null() {
//         unsafe { let _ = Box::from_raw(ctx_ptr); }
//         println!("RenderContext destroyed.");
//     }
// }
//
// #[unsafe(no_mangle)]
// pub extern "C" fn render(ctx_ptr: *mut Context, buffer: *mut u8) {
//
//     if ctx_ptr.is_null() || buffer.is_null() { return; }
//
//     let ctx = unsafe { &mut *ctx_ptr };
//
//     ctx.render_context.set_paint(css::BLACK);
//     ctx.render_context.fill_rect(&Rect::from_points((0., 0.), (ctx.width as f64, ctx.height as f64)));
//
//     ctx.render_context.set_paint(css::MAGENTA);
//     ctx.render_context.fill_rect(&Rect::from_points((10., 10.), (110., 110.)));
//
//     let handle = SystemSource::new()
//         .select_best_match(&[FamilyName::SansSerif], &Properties::new())
//         .expect("not found system font");
//     let font = handle.load()
//         .expect("failed to load font data");
//     let font_data: Arc<Vec<u8>> = font.copy_font_data()
//         .expect("failed to copy font data");
//
//
//     let font_ref = FontRef::from_index(&font_data, 0)
//         .expect("failed to load font data");
//
//     let font_size = 24.;
//     let charmap = font_ref.charmap();
//     let glyph_metrics = font_ref.glyph_metrics(
//         skrifa::instance::Size::new(font_size),
//         skrifa::instance::LocationRef::default(),
//     );
//
//     let text = "Hello, world!";
//     let mut cursor_x = 30.;
//     let baseline_y = 30.;
//
//
//     let glyphs: Vec<Glyph> = text
//         .chars()
//         .map(|ch| {
//             let gid = charmap.map(ch).unwrap_or_default();
//             let x = cursor_x;
//             let advance = glyph_metrics.advance_width(gid).unwrap_or(font_size * 0.5);
//             cursor_x += advance;
//
//             Glyph {
//                 id: gid.to_u32(),
//                 x,
//                 y: baseline_y,
//             }
//         })
//         .collect();
//
//     let font_blob = Blob::new(font_data);
//     let font = FontData::new(font_blob, 0);
//
//     ctx.render_context.set_paint(css::WHITE);
//     let builder = ctx.render_context.glyph_run(&mut ctx.resources, &font);
//     builder.font_size(font_size).fill_glyphs(glyphs.into_iter());
//
//     ctx.render_context.flush();
//     ctx.render_context.render(&mut ctx.pixmap, &mut ctx.resources);
//
//     let length = (ctx.width * ctx.height * 4) as usize;
//     let src_bytes = unsafe { slice::from_raw_parts(ctx.pixmap.data_as_u8_slice().as_ptr(), length) };
//     let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, length) };
//
//     for (src, dst) in src_bytes.chunks_exact(4).zip(dst_bytes.chunks_exact_mut(4)) {
//         dst[0] = src[2]; // B <- R
//         dst[1] = src[1]; // G <- G
//         dst[2] = src[0]; // R <- B
//         dst[3] = src[3]; // A <- A
//     }
//
// }
