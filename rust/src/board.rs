use crate::piece::Color;
use crate::piece::Piece;
use crate::piece::PieceType;

use std::collections::HashMap;
use std::cmp;
use std::fmt;

#[derive(Copy, Clone, PartialEq)]
pub enum MoveFailure {
    NotTurn,
    CantTakeOwnPieces,
    CantMoveThere,
    InCheck,
}

#[derive(Copy, Clone)]
pub struct Point {
    pub col: usize,
    pub row: usize,
}

impl fmt::Display for Point {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "({}, {})", self.row, self.col)
    }
}

pub struct Board {
    // Holds a 2d array of the pieces of the board.
    // The first index is the row, the second is the column.
    pieces: [[Piece; 8]; 8],

    // Boolean of whose turn it is
    turn: bool,

    // If a pawn made a double move forward last move,
    // then this holds that piece. Used for Enpassant.
    double_move_pawn: Option<Piece>,

    // Counter checking if a stalemate has occured.
    stalemate_counter: u16,

    // Hashmap that holds copies of the board. If there is ever a value of 3
    // in one of the bucks the game is a draw.
    three_repetition: HashMap<[[Piece; 8]; 8], i32>,
}

impl Board {
    pub fn new() -> Self {
        let three_repetition = HashMap::new();
        let stalemate_counter = 0;
        let double_move_pawn = None; 

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

        Board { pieces, turn, three_repetition, stalemate_counter, double_move_pawn }
    }

    fn can_king_move(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        None
    }

    fn can_knight_move(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        if ((start_point.row as i32 - end_point.row as i32).abs() == 1
            && (start_point.col as i32 - end_point.col as i32).abs() == 2)
            || ((start_point.row as i32 - end_point.row as i32).abs() == 2
                && (start_point.col as i32 - end_point.col as i32).abs() == 1) {
                    None 
                } else {
                    Some(MoveFailure::CantMoveThere)
                }
    }

    fn is_empty(&self, point: Point) -> bool {
        self.pieces[point.row][point.col].piece_type == PieceType::Empty
    }

    fn enpassant_check(&self, start_point: Point, end_point: Point) -> bool {
        true
    }

    fn can_pawn_move(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        let piece_color = self.pieces[start_point.row][start_point.col].color;
        // Make sure the pawns are moving in the right direction
        if (piece_color == Color::White && start_point.col <= end_point.col)
            || (piece_color == Color::Black && start_point.col >= end_point.col)
        {
            return Some(MoveFailure::CantMoveThere);
        }

        // Movement
        if start_point.col == end_point.col {
            // Can't take a piece from normal movement
            if !self.is_empty(end_point) {
                return Some(MoveFailure::CantMoveThere);
            }
            // Normal one space move forward
            if (start_point.row as i32 - end_point.row as i32).abs() == 1 {
                return None;
            }
            // Double move forward if on starting line
            if (start_point.row as i32 - end_point.row as i32).abs() == 2 {
                if piece_color == Color::White && self.pieces[end_point.row + 1][end_point.col].is_empty() {
                    return None;
                } else if piece_color == Color::Black && self.pieces[end_point.row - 1][end_point.col].is_empty(){
                    return None;
                }
            }
        }
        // Taking a Piece
        else if (start_point.col as i32 - end_point.col as i32).abs() == 1 && ((start_point.row as i32 - end_point.row as i32).abs() == 1
        && !self.is_empty(end_point))
        || self.enpassant_check(start_point, end_point) {
            return None;
        }
        Some(MoveFailure::CantMoveThere)
    }

    fn diagonal_movement_check(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        if ((start_point.row as i32 - end_point.row as i32).abs()
            == (start_point.col as i32 - end_point.col as i32).abs())
            && self.diagonal_check_for_pieces(start_point, end_point) {
                None
            } else {
                Some(MoveFailure::CantMoveThere)
            }
    }

    fn diagonal_check_for_pieces(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        let min_row = cmp::min(start_point.row, end_point.row);
        let max_row = cmp::max(start_point.row, end_point.row);
        let min_col = cmp::min(start_point.col, end_point.col);
        let max_col = cmp::max(start_point.col, end_point.col);
        if end_point.col - end_point.row == start_point.col - start_point.row {
            for (row, col) in (min_row + 1 .. max_row).zip(min_col + 1 .. max_col) {
                if !self.is_empty(Point {row, col}) {
                    return Some(MoveFailure::CantMoveThere);
                }
            }
        } else {
            for (row, col) in (min_row + 1 .. max_row).zip((min_col + 1 .. max_col).rev()) {
                if !self.is_empty(Point {row, col}) {
                    return Some(MoveFailure::CantMoveThere);
                }
            }
        }
        None
    }

    fn horizontal_movement_check(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        if ((start_point.col != end_point.col && start_point.row == end_point.row)
            || (start_point.row != end_point.row && start_point.col != end_point.col))
            && self.horizontal_check_for_pieces(start_point, end_point) {
                None
            } else {
                Some(MoveFailure::CantMoveThere);
            }
    }

    fn horizontal_check_for_pieces(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        if start_point.col == end_point.col {
            let min_row = cmp::min(start_point.row, end_point.row);
            let max_row = cmp::max(start_point.row, end_point.row);
            for row in min_row + 1..max_row {
                if !self.is_empty(Point { row: row, col: start_point.col}) {
                    return Some(MoveFailure::CantMoveThere);
                }
            }
        } else {
            let min_col = cmp::min(start_point.col, end_point.col);
            let max_col = cmp::max(start_point.col, end_point.col);
            for col in min_col + 1..max_col {
                if !self.is_empty(Point { row: start_point.row, col: col}) {
                    return Some(MoveFailure::CantMoveThere);
                }
            }
        }
        None
    }

    fn can_piece_move(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        let start_piece = &self.pieces[start_point.row][start_point.col];
        let end_piece = &self.pieces[end_point.row][end_point.col];

        if start_piece.color == end_piece.color {
            return Some(MoveFailure::CantTakeOwnPieces);
        }


        // Do I want all of these subfunction returning a simple boolean? Or do I want to make them an option<MoveFailure> to give the ability in the future
        // to add some return like values?
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

    fn can_move(&self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        let start_piece = &self.pieces[start_point.row][start_point.col];
        let end_piece = &self.pieces[end_point.row][end_point.col];
        if !self.is_turn(start_piece) {
            Some(MoveFailure::NotTurn)
        } else {
            self.can_piece_move(start_point, end_point)
        }
    }

    pub fn make_move(&mut self, start_point: Point, end_point: Point) -> bool {
        let can_move = self.can_move(start_point, end_point)
        match can_move {
            None => {
                self.pieces[end_point.row][end_point.col] =
                self.pieces[start_point.row][start_point.col];
                self.pieces[start_point.row][start_point.col] = Piece {
                    piece_type: PieceType::Empty,
                    color: Color::None,
                };
                self.turn = !self.turn;
                true
            },
            _ => false
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
