use std::fmt;

#[derive(Copy, Clone)]
enum Color {
    White,
    Black,
    None,
}

#[derive(Copy, Clone, PartialEq)]
enum PieceType {
    Bishop,
    Empty,
    King,
    Knight,
    Pawn,
    Queen,
    Rook,
}

#[derive(Copy, Clone)]
pub struct Point {
    pub col: usize,
    pub row: usize,
}

#[derive(Copy, Clone)]
struct Piece {
    piece_type: PieceType,
    color: Color,
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

pub struct Board {
    // Holds a 2d array of the pieces of the board.
    // The first index is the column, the second is the row.
    pieces: [[Piece; 8]; 8],

    // Boolean of whose turn it is
    turn: bool,
}

impl Board {
    pub fn new() -> Self {
        let mut pieces = [[Piece {
            piece_type: PieceType::Empty,
            color: Color::None,
        }; 8]; 8];
        let turn = true;

        pieces[0][0] = Piece {
            piece_type: PieceType::Rook,
            color: Color::White,
        };
        pieces[0][1] = Piece {
            piece_type: PieceType::Knight,
            color: Color::White,
        };
        pieces[0][2] = Piece {
            piece_type: PieceType::Bishop,
            color: Color::White,
        };
        pieces[0][3] = Piece {
            piece_type: PieceType::Queen,
            color: Color::White,
        };
        pieces[0][4] = Piece {
            piece_type: PieceType::King,
            color: Color::White,
        };
        pieces[0][5] = Piece {
            piece_type: PieceType::Bishop,
            color: Color::White,
        };
        pieces[0][6] = Piece {
            piece_type: PieceType::Knight,
            color: Color::White,
        };
        pieces[0][7] = Piece {
            piece_type: PieceType::Rook,
            color: Color::White,
        };

        pieces[7][0] = Piece {
            piece_type: PieceType::Rook,
            color: Color::Black,
        };
        pieces[7][1] = Piece {
            piece_type: PieceType::Knight,
            color: Color::Black,
        };
        pieces[7][2] = Piece {
            piece_type: PieceType::Bishop,
            color: Color::Black,
        };
        pieces[7][3] = Piece {
            piece_type: PieceType::Queen,
            color: Color::Black,
        };
        pieces[7][4] = Piece {
            piece_type: PieceType::King,
            color: Color::Black,
        };
        pieces[7][5] = Piece {
            piece_type: PieceType::Bishop,
            color: Color::Black,
        };
        pieces[7][6] = Piece {
            piece_type: PieceType::Knight,
            color: Color::Black,
        };
        pieces[7][7] = Piece {
            piece_type: PieceType::Rook,
            color: Color::Black,
        };

        for row in 0..8 {
            pieces[1][row] = Piece {
                piece_type: PieceType::Pawn,
                color: Color::White,
            };
            pieces[6][row] = Piece {
                piece_type: PieceType::Pawn,
                color: Color::Black,
            };
        }

        Board { pieces, turn }
    }

    fn can_bishop_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_king_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_knight_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_pawn_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_queen_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_rook_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_piece_move(&self, start_point: Point, end_point: Point) -> bool {
        let start_piece = &self.pieces[start_point.col][start_point.row];
        let end_piece = &self.pieces[end_point.col][end_point.row];

        match &start_piece.piece_type {
            PieceType::Bishop => self.can_bishop_move(start_point, end_point),
            PieceType::Empty => false,
            PieceType::King => self.can_king_move(start_point, end_point),
            PieceType::Knight => self.can_knight_move(start_point, end_point),
            PieceType::Pawn => self.can_pawn_move(start_point, end_point),
            PieceType::Queen => self.can_queen_move(start_point, end_point),
            PieceType::Rook => self.can_rook_move(start_point, end_point),
        }
    }

    fn is_turn(&self, piece: &Piece) -> bool {
        match &piece.color {
            Color::White => self.turn,
            Color::Black => !self.turn,
            Color::None => false,
        }
    }

    fn can_move(&self, start_point: Point, end_point: Point) -> bool {
        let start_piece = &self.pieces[start_point.col][start_point.row];
        let end_piece = &self.pieces[end_point.col][end_point.row];
        let mut can_move = self.is_turn(start_piece);
        can_move &= self.can_piece_move(start_point, end_point);
        can_move
    }

    pub fn make_move(&mut self, start_point: Point, end_point: Point) -> bool {
        if self.can_move(start_point, end_point) {
            self.pieces[end_point.col][end_point.row] =
                self.pieces[start_point.col][start_point.row];
            self.pieces[start_point.col][start_point.row] = Piece {
                piece_type: PieceType::Empty,
                color: Color::None,
            };
            true
        } else {
            false
        }
    }
}

impl fmt::Display for Board {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let mut string_output: String = String::from("");
        for row in self.pieces.iter() {
            for piece in row {
                string_output = string_output + &piece.to_string() + " ";
            }
            string_output = string_output + "\n";
        }
        write!(f, "{}", string_output)
    }
}
