use std::fmt;
use std::cmp;

#[derive(Copy, Clone, PartialEq)]
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
    // The first index is the row, the second is the column.
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

    fn can_king_move(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_knight_move(&self, start_point: Point, end_point: Point) -> bool {
        ((start_point.row as i32 - end_point.row as i32).abs() == 1
            && (start_point.col as i32 - end_point.col as i32).abs() == 2)
            || ((start_point.row as i32 - end_point.row as i32).abs() == 2
                && (start_point.col as i32 - end_point.col as i32).abs() == 1)
    }

    fn is_empty(&self, point: Point) -> bool {
        self.pieces[point.row][point.col].piece_type == PieceType::Empty
    }

    fn enpassant_check(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_pawn_move(&self, start_point: Point, end_point: Point) -> bool {
        let piece_color = self.pieces[start_point.row][start_point.col].color;
        // Make sure the pawns are moving in the right direction
        if (piece_color == Color::White && start_point.col <= end_point.col)
            || (piece_color == Color::Black && start_point.col >= end_point.col)
        {
            return false;
        }

        // Movement
        if start_point.col == end_point.col {
            // Can't take a piece from normal movement
            if !self.is_empty(end_point) {
                return false;
            }
            // Normal one space move forward
            if (start_point.row as i32 - end_point.row as i32).abs() == 1 {
                return true;
            }
            // Double move forward if on starting line
            if (start_point.row as i32 - end_point.row as i32).abs() == 2 {
                if piece_color == Color::White {
                    return self.is_empty(Point {
                        row: end_point.row + 1,
                        col: end_point.col,
                    });
                } else if piece_color == Color::Black {
                    return self.is_empty(Point {
                        row: end_point.row - 1,
                        col: end_point.col,
                    });
                }
            }
        }
        // Taking a Piece
        else if (start_point.col as i32 - end_point.col as i32).abs() == 1 {
            return ((start_point.row as i32 - end_point.row as i32).abs() == 1
                && !self.is_empty(end_point))
                || self.enpassant_check(start_point, end_point);
        }
        false
    }

    fn diagonal_movement_check(&self, start_point: Point, end_point: Point) -> bool {
        ((start_point.row as i32 - end_point.row as i32).abs()
            == (start_point.col as i32 - end_point.col as i32).abs())
            && self.diagonal_check_for_pieces(start_point, end_point)
    }

    fn diagonal_check_for_pieces(&self, start_point: Point, end_point: Point) -> bool {
        let min_row = cmp::min(start_point.row, end_point.row);
        let max_row = cmp::max(start_point.row, end_point.row);
        let min_col = cmp::min(start_point.col, end_point.col);
        let max_col = cmp::max(start_point.col, end_point.col);
        if end_point.col - end_point.row == start_point.col - start_point.row {
            for (row, col) in (min_row + 1 .. max_row).zip(min_col + 1 .. max_col) {
                if !self.is_empty(Point {row, col}) {
                    return false;
                }
            }
        } else {
            for (row, col) in (min_row + 1 .. max_row).zip((min_col + 1 .. max_col).rev()) {
                if !self.is_empty(Point {row, col}) {
                    return false;
                }
            }
        }
        true
    }

    fn horizontal_movement_check(&self, start_point: Point, end_point: Point) -> bool {
        ((start_point.col != end_point.col && start_point.row == end_point.row)
            || (start_point.row != end_point.row && start_point.col != end_point.col))
            && self.horizontal_check_for_pieces(start_point, end_point)
    }

    fn horizontal_check_for_pieces(&self, start_point: Point, end_point: Point) -> bool {
        if start_point.col == end_point.col {
            let min_row = cmp::min(start_point.row, end_point.row);
            let max_row = cmp::max(start_point.row, end_point.row);
            for row in min_row + 1..max_row {
                if !self.is_empty(Point { row: row, col: start_point.col}) {
                    return false;
                }
            }
        } else {
            let min_col = cmp::min(start_point.col, end_point.col);
            let max_col = cmp::max(start_point.col, end_point.col);
            for col in min_col + 1..max_col {
                if !self.is_empty(Point { row: start_point.row, col: col}) {
                    return false;
                }
            }
        }
        true
    }

    fn can_piece_move(&self, start_point: Point, end_point: Point) -> bool {
        let start_piece = &self.pieces[start_point.col][start_point.row];
        let end_piece = &self.pieces[end_point.col][end_point.row];

        if start_piece.color == end_piece.color {
            println!("Can't take your own piece.");
            return false;
        }

        match &start_piece.piece_type {
            PieceType::Bishop => self.diagonal_movement_check(start_point, end_point),
            PieceType::Empty => false,
            PieceType::King => self.can_king_move(start_point, end_point),
            PieceType::Knight => self.can_knight_move(start_point, end_point),
            PieceType::Pawn => self.can_pawn_move(start_point, end_point),
            PieceType::Queen => {
                self.diagonal_movement_check(start_point, end_point)
                    || self.horizontal_movement_check(start_point, end_point)
            }
            PieceType::Rook => self.horizontal_movement_check(start_point, end_point),
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
        let start_piece = &self.pieces[start_point.row][start_point.col];
        let end_piece = &self.pieces[end_point.row][end_point.col];
        let mut can_move = self.is_turn(start_piece);
        can_move &= self.can_piece_move(start_point, end_point);
        can_move
    }

    pub fn make_move(&mut self, start_point: Point, end_point: Point) -> bool {
        if self.can_move(start_point, end_point) {
            self.pieces[end_point.row][end_point.col] =
                self.pieces[start_point.row][start_point.col];
            self.pieces[start_point.row][start_point.col] = Piece {
                piece_type: PieceType::Empty,
                color: Color::None,
            };
            self.turn = !self.turn;
            true
        } else {
            false
        }
    }
}

impl fmt::Display for Board {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let mut string_output: String = String::from("");
        for row in 0..8 {
            string_output = string_output + &(8 - row).to_string() + " | ";
            for col in 0..8 {
                string_output = string_output + &self.pieces[7 - row][7 - col].to_string() + " ";
            }
            string_output = string_output + "\n";
        }
        string_output = string_output + "    -----------------------\n";
        string_output = string_output + "    A  B  C  D  E  F  G  H";
        write!(f, "{}", string_output)
    }
}
