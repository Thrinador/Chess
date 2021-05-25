use std::fmt;

#[derive(Copy, Clone, PartialEq)]
pub enum Color {
    White,
    Black,
    None,
}

#[derive(Copy, Clone, PartialEq)]
pub enum PieceType {
    Bishop,
    Empty,
    King,
    Knight,
    Pawn,
    Queen,
    Rook,
}

#[derive(Copy, Clone)]
pub struct Piece {
    pub piece_type: PieceType,
    pub color: Color,
}

impl Piece {
    pub fn is_empty(&self) -> bool {
        self.piece_type == PieceType::Empty
    }
}

impl fmt::Display for Piece {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let string_color = match &self.color {
            Color::Black => "B",
            Color::White => "W",
            Color::None => " ",
        };
        let string_type = match &self.piece_type {
            PieceType::Bishop => "B",
            PieceType::Empty => " ",
            PieceType::King => "K",
            PieceType::Knight => "N",
            PieceType::Pawn => "P",
            PieceType::Queen => "Q",
            PieceType::Rook => "R",
        };
        write!(f, "{}{}", string_color, string_type)
    }
}