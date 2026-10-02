use std::slice;
use vello::{
    kurbo::{Affine, Rect},
    peniko::{Color, Fill},
    Scene,
};
use vello::kurbo::Circle;
use vello::wgpu;

pub struct RenderContext {
    device: wgpu::Device,
    queue: wgpu::Queue,
    renderer: vello::Renderer,
    texture: wgpu::Texture,
    readback_buffer: wgpu::Buffer,
    width: u32,
    height: u32,
    scene: Scene
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
        scene
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


#[unsafe(no_mangle)]
pub extern "C" fn fill_rect(ctx_ptr: *mut RenderContext,
        x: f64, y: f64, width: f64, height: f64,
        r: u8, g: u8, b: u8, a: u8) {
    if ctx_ptr.is_null() { return; }
    let ctx = unsafe { &mut *ctx_ptr };
    let scene = &mut ctx.scene;
    scene.fill(
        Fill::NonZero,
        Affine::IDENTITY,
        Color::from_rgba8(r, g, b, a),
        None,
        &Rect::new(x, y, width, height),
    );
}
