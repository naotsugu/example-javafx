
#[unsafe(no_mangle)]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}

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
}
