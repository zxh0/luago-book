pub mod chunk;
mod reader;

pub fn undump(data: Vec<u8>) -> chunk::Prototype {
    let mut r = reader::Reader::new(data);
    r.check_header();
    r.read_byte(); // size_upvalues
    r.read_proto()
}
