extern crate photon_rs;

// use photon_rs::{channels, colour_spaces, conv, effects, filters, multiple};
use photon_rs::{filters, transform};
use photon_rs::native::{open_image_from_bytes, image_to_bytes};

use photon_rs::monochrome;

use std::mem;
use std::slice;
use std::str;

#[no_mangle]
pub extern "C" fn alloc(len: u32) -> *mut u8 {
    let mut buffer: Vec<u8> = Vec::with_capacity(len as usize);
    let pointer: *mut u8 = buffer.as_mut_ptr();
    mem::forget(buffer);
    pointer
}

#[no_mangle]
pub unsafe extern "C" fn dealloc(ptr: *mut u8, len: u32) {
    let _ = Vec::from_raw_parts(ptr, 0, len as usize);
}

// Applies sepia and returns packed u64: high 32 bits = len, low 32 bits = ptr
#[no_mangle]
pub extern "C" fn apply_monochrome(
    img_ptr: u32,
    img_len: u32,
    output_ptr_ptr: u32,
    output_len_ptr: u32) -> u32 {
    let input_bytes: &[u8] = unsafe { slice::from_raw_parts(img_ptr as usize as *const u8, img_len as usize) };
    let mut image = open_image_from_bytes(input_bytes).expect("failed to decode input image");

    monochrome::sepia(&mut image);

    // Encode result as PNG bytes using photon-rs base64 helper
    let b64 = image.get_base64();
    let b64_data = match b64.split(',').nth(1) { Some(s) => s, None => &b64 };
    let output_bytes: Vec<u8> = photon_rs::base64_to_vec(b64_data);
    write_out(&output_bytes, output_ptr_ptr, output_len_ptr);
    0
}

// Helper: write a byte slice to a freshly allocated region using `allocate`,
// then store its pointer/len at the given output addrs.
fn write_out(buf: &[u8], output_ptr_ptr: u32, output_len_ptr: u32) {
    // Record length
    unsafe { (output_len_ptr as *mut u32).write_unaligned(buf.len() as u32); }

    if buf.is_empty() {
        unsafe { (output_ptr_ptr as *mut u32).write_unaligned(0) };
        return;
    }

    // Allocate exact size (we rely on `allocate`’s page-granularity and just use the start)
    let out_ptr = alloc(buf.len() as u32);
    unsafe { (output_ptr_ptr as *mut u32).write_unaligned(out_ptr as usize as u32) };

    // Copy bytes
    let dst = unsafe { slice::from_raw_parts_mut(out_ptr as *mut u8, buf.len()) };
    dst.copy_from_slice(buf);
}

// Applies a named filter and writes PNG bytes to provided out-pointers (same contract as `apply_monochrome`).
// Available filters (from `photon_rs::filters`):
// "oceanic", "islands", "marine", "seagreen", "flagblue", "liquid", "diamante",
// "radio", "twenties", "rosetint", "mauve", "bluechrome", "vintage", "perfume",
// "serenity", "golden", "pastel_pink", "cali", "dramatic", "firenze", "obsidian",
// "lofi".
#[no_mangle]
pub extern "C" fn apply_effect(
    img_ptr: u32,
    img_len: u32,
    effect_ptr: u32,
    effect_len: u32,
    output_ptr_ptr: u32,
    output_len_ptr: u32,
) -> u32 {
    let input_bytes: &[u8] = unsafe { slice::from_raw_parts(img_ptr as usize as *const u8, img_len as usize) };
    let effect_bytes: &[u8] = unsafe { slice::from_raw_parts(effect_ptr as usize as *const u8, effect_len as usize) };

    let effect_name = match str::from_utf8(effect_bytes) {
        Ok(s) => s.trim().to_ascii_lowercase(),
        Err(_) => return 1,
    };

    let mut image = match open_image_from_bytes(input_bytes) {
        Ok(img) => img,
        Err(_) => return 2,
    };

    filters::filter(&mut image, effect_name.as_str());

    let b64 = image.get_base64();
    let b64_data = match b64.split(',').nth(1) { Some(s) => s, None => &b64 };
    let output_bytes: Vec<u8> = photon_rs::base64_to_vec(b64_data);
    write_out(&output_bytes, output_ptr_ptr, output_len_ptr);
    0
}

// Applies a transformation and writes PNG bytes to provided out-pointers (same contract as `apply_effect`).
// Available transformations:
// "fliph" - flip horizontally
// "flipv" - flip vertically  
// "resize" - resize image (requires width and height parameters)
// "crop" - crop image (requires x1, y1, x2, y2 parameters)
// "padding_uniform" - add uniform padding (requires padding amount and color)
// "padding_left", "padding_right", "padding_top", "padding_bottom" - directional padding
#[no_mangle]
pub extern "C" fn apply_transformation(
    img_ptr: u32,
    img_len: u32,
    transformation_ptr: u32,
    transformation_len: u32,
    output_ptr_ptr: u32,
    output_len_ptr: u32,
) -> u32 {
    let input_bytes: &[u8] = unsafe { slice::from_raw_parts(img_ptr as usize as *const u8, img_len as usize) };
    let transformation_bytes: &[u8] = unsafe { slice::from_raw_parts(transformation_ptr as usize as *const u8, transformation_len as usize) };

    let transformation_name = match str::from_utf8(transformation_bytes) {
        Ok(s) => s.trim().to_ascii_lowercase(),
        Err(_) => return 1,
    };

    let mut image = match open_image_from_bytes(input_bytes) {
        Ok(img) => img,
        Err(_) => return 2,
    };

    // Apply the transformation based on the name
    match transformation_name.as_str() {
        "fliph" => {
            transform::fliph(&mut image);
        },
        "flipv" => {
            transform::flipv(&mut image);
        },
        "resize" => {
            // For resize, we'll use default dimensions (half the original size)
            // In a real implementation, you might want to pass these as parameters
            let new_width = image.get_width() / 2;
            let new_height = image.get_height() / 2;
            let resized_image = transform::resize(&image, new_width, new_height, transform::SamplingFilter::CatmullRom);
            image = resized_image;
        },
        "crop" => {
            // For crop, we'll crop to the center quarter of the image
            // In a real implementation, you might want to pass these as parameters
            let width = image.get_width();
            let height = image.get_height();
            let x1 = width / 4;
            let y1 = height / 4;
            let x2 = width - width / 4;
            let y2 = height - height / 4;
            let cropped_image = transform::crop(&mut image, x1, y1, x2, y2);
            image = cropped_image;
        },
        "padding_uniform" => {
            // Add 20 pixels of white padding
            let padding_color = photon_rs::Rgba::new(255, 255, 255, 255);
            let padded_image = transform::padding_uniform(&image, 20, padding_color);
            image = padded_image;
        },
        "padding_left" => {
            let padding_color = photon_rs::Rgba::new(255, 255, 255, 255);
            let padded_image = transform::padding_left(&image, 20, padding_color);
            image = padded_image;
        },
        "padding_right" => {
            let padding_color = photon_rs::Rgba::new(255, 255, 255, 255);
            let padded_image = transform::padding_right(&image, 20, padding_color);
            image = padded_image;
        },
        "padding_top" => {
            let padding_color = photon_rs::Rgba::new(255, 255, 255, 255);
            let padded_image = transform::padding_top(&image, 20, padding_color);
            image = padded_image;
        },
        "padding_bottom" => {
            let padding_color = photon_rs::Rgba::new(255, 255, 255, 255);
            let padded_image = transform::padding_bottom(&image, 20, padding_color);
            image = padded_image;
        },
        _ => {
            return 3; // Invalid transformation name
        }
    }

    let b64 = image.get_base64();
    let b64_data = match b64.split(',').nth(1) { Some(s) => s, None => &b64 };
    let output_bytes: Vec<u8> = photon_rs::base64_to_vec(b64_data);
    write_out(&output_bytes, output_ptr_ptr, output_len_ptr);
    0
}
