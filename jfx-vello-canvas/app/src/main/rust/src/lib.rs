
#[unsafe(no_mangle)]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}

use std::slice;
use vello_cpu::{RenderContext, Resources, Pixmap};
use vello_cpu::{color::{palette::css, PremulRgba8}, kurbo::Rect};

pub struct Context {
    render_context: RenderContext,
    resources: Resources,
    pixmap: Pixmap,
    width: u32,
    height: u32,
}

#[unsafe(no_mangle)]
pub extern "C" fn create_ctx(width: u32, height: u32) -> *mut Context {
    let ctx = Context {
        render_context: RenderContext::new(width as u16, height as u16),
        resources: Resources::new(),
        pixmap: Pixmap::new(width as u16, height as u16),
        width, height
    };
    Box::into_raw(Box::new(ctx))
}

#[unsafe(no_mangle)]
pub extern "C" fn destroy_ctx(ctx_ptr: *mut Context) {
    if !ctx_ptr.is_null() {
        unsafe { let _ = Box::from_raw(ctx_ptr); }
        println!("RenderContext destroyed.");
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn render(ctx_ptr: *mut Context, buffer: *mut u8) {

    if ctx_ptr.is_null() || buffer.is_null() { return; }

    let ctx = unsafe { &mut *ctx_ptr };
    let length = (ctx.width * ctx.height * 4) as usize;
    ctx.render_context.set_paint(css::MAGENTA);
    ctx.render_context.fill_rect(&Rect::from_points((10., 10.), (110., 110.)));
    ctx.render_context.flush();
    ctx.render_context.render(&mut ctx.pixmap, &mut ctx.resources);

    let src_bytes = unsafe { slice::from_raw_parts(ctx.pixmap.data_as_u8_slice().as_ptr(), length) };
    let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, length) };

    for (src, dst) in src_bytes.chunks_exact(4).zip(dst_bytes.chunks_exact_mut(4)) {
        dst[0] = src[2]; // B <- R
        dst[1] = src[1]; // G <- G
        dst[2] = src[0]; // R <- B
        dst[3] = src[3]; // A <- A
    }

}
