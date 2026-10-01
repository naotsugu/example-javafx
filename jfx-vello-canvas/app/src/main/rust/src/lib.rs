
#[unsafe(no_mangle)]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}

use std::slice;
use vello_cpu::{RenderContext, Resources, Pixmap, Glyph};
use vello_cpu::{color::{palette::css, PremulRgba8}, kurbo::Rect};
use std::sync::Arc;
use skrifa::{FontRef, MetadataProvider};
use vello_cpu::peniko::{Blob, FontData, Color, Fill};

const FONT_DATA: &[u8] = include_bytes!("/System/Library/Fonts/ヒラギノ角ゴシック W3.ttc");

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

    ctx.render_context.set_paint(css::BLACK);
    ctx.render_context.fill_rect(&Rect::from_points((0., 0.), (ctx.width as f64, ctx.height as f64)));

    ctx.render_context.set_paint(css::MAGENTA);
    ctx.render_context.fill_rect(&Rect::from_points((10., 10.), (110., 110.)));

    ctx.render_context.set_paint(css::WHITE);
    let font_ref = FontRef::from_index(FONT_DATA, 0).expect("failed to load font data");
    let charmap = font_ref.charmap();
    let font_size = 14.0;
    let glyph_metrics = font_ref.glyph_metrics(
        skrifa::instance::Size::new(font_size),
        skrifa::instance::LocationRef::default(),
    );

    let text = "Hello, vello_cpu! 日本語フォントレンダリング品質";
    let mut cursor_x = 50.0;
    let baseline_y = 150.0;

    let glyphs: Vec<Glyph> = text
        .chars()
        .map(|ch| {
            let gid = charmap.map(ch).unwrap_or_default();
            let x = cursor_x;
            let advance = glyph_metrics.advance_width(gid).unwrap_or(font_size * 0.5);
            cursor_x += advance;

            Glyph {
                id: gid.to_u32(),
                x,
                y: baseline_y,
            }
        })
        .collect();

    let font_blob = Blob::new(Arc::new(FONT_DATA));
    let font = FontData::new(font_blob, 0);
    let builder = ctx.render_context.glyph_run(&mut ctx.resources, &font);
    builder.font_size(font_size).hint(true).fill_glyphs(glyphs.into_iter());

    ctx.render_context.flush();
    ctx.render_context.render(&mut ctx.pixmap, &mut ctx.resources);

    let length = (ctx.width * ctx.height * 4) as usize;
    let src_bytes = unsafe { slice::from_raw_parts(ctx.pixmap.data_as_u8_slice().as_ptr(), length) };
    let dst_bytes = unsafe { slice::from_raw_parts_mut(buffer, length) };

    for (src, dst) in src_bytes.chunks_exact(4).zip(dst_bytes.chunks_exact_mut(4)) {
        dst[0] = src[2]; // B <- R
        dst[1] = src[1]; // G <- G
        dst[2] = src[0]; // R <- B
        dst[3] = src[3]; // A <- A
    }

}
