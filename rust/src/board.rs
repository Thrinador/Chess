use crate::piece::Color;
use crate::piece::Piece;
use crate::piece::PieceType;

use std::cmp;
use std::collections::HashMap;
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
    // then this holds that piece location. Used for Enpassant.
    double_move_pawn: Option<Point>,

    // Counter checking if a stalemate has occured.
    stalemate_counter: u16,

    // Hashmap that holds copies of the board. If there is ever a value of 3
    // in one of the bucks the game is a draw.
    three_repetition: HashMap<[[Piece; 8]; 8], i32>,

    // If an enpassant occured, then there will be a piece that needs to be removed.
    enpassant_piece_to_remove: Option<Point>,
}

impl Board {
    pub fn new() -> Self {
        let three_repetition = HashMap::new();
        let stalemate_counter = 0;
        let double_move_pawn = None;
        let enpassant_piece_to_remove = None;

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

        Board {
            pieces,
            turn,
            three_repetition,
            stalemate_counter,
            double_move_pawn,
            enpassant_piece_to_remove,
        }
    }

    fn get_piece(&self, point: Point) -> &Piece {
        &self.pieces[point.row][point.col]
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

    fn enpassant_check(&mut self, start_point: Point, end_point: Point) -> bool {
        // Can enpassant only if the last move was a double pawn move
        if let Some(other_point) = self.double_move_pawn {
            // Check that the pawn moved in the appropriate direction only one square
            if (end_point.row as i32 - other_point.row as i32).abs() == 1
                && (start_point.col as i32 - other_point.col as i32).abs() == 1
                && end_point.col == other_point.col
                && start_point.row == other_point.row
            {
                self.enpassant_piece_to_remove = Some(other_point);
                return true;
            }
        }
        false
    }

    fn can_pawn_move(&mut self, start_point: Point, end_point: Point) -> bool {
        let piece_color = self.pieces[start_point.row][start_point.col].color;
        // Make sure the pawns are moving in the right direction
        if (piece_color == Color::White && start_point.row >= end_point.row)
            || (piece_color == Color::Black && start_point.row <= end_point.row)
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
                if piece_color == Color::White
                    && self.pieces[end_point.row + 1][end_point.col].is_empty()
                {
                    return true;
                } else if piece_color == Color::Black
                    && self.pieces[end_point.row - 1][end_point.col].is_empty()
                {
                    return true;
                }
            }
        }
        // Taking a Piece
        else if (start_point.col as i32 - end_point.col as i32).abs() == 1
            && (start_point.row as i32 - end_point.row as i32).abs() == 1
            && !self.is_empty(end_point)
        {
            return true;
        } else if self.enpassant_check(start_point, end_point) {
            return true;
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
            for (row, col) in (min_row + 1..max_row).zip(min_col + 1..max_col) {
                if !self.is_empty(Point { row, col }) {
                    return false;
                }
            }
        } else {
            for (row, col) in (min_row + 1..max_row).zip((min_col + 1..max_col).rev()) {
                if !self.is_empty(Point { row, col }) {
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
                if !self.is_empty(Point {
                    row: row,
                    col: start_point.col,
                }) {
                    return false;
                }
            }
        } else {
            let min_col = cmp::min(start_point.col, end_point.col);
            let max_col = cmp::max(start_point.col, end_point.col);
            for col in min_col + 1..max_col {
                if !self.is_empty(Point {
                    row: start_point.row,
                    col: col,
                }) {
                    return false;
                }
            }
        }
        true
    }

    fn can_piece_move(&mut self, start_point: Point, end_point: Point) -> bool {
        match self.get_piece(start_point).piece_type {
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

    fn can_move(&mut self, start_point: Point, end_point: Point) -> Option<MoveFailure> {
        let start_piece = &self.pieces[start_point.row][start_point.col];
        let end_piece = &self.pieces[end_point.row][end_point.col];

        if !self.is_turn(start_piece) {
            Some(MoveFailure::NotTurn)
        } else if start_piece.color == end_piece.color {
            return Some(MoveFailure::CantTakeOwnPieces);
        } else if !self.can_piece_move(start_point, end_point) {
            Some(MoveFailure::CantMoveThere)
        } else {
            None
        }
    }

    pub fn make_move(&mut self, start_point: Point, end_point: Point) -> bool {
        if let Some(failure) = self.can_move(start_point, end_point) {
            match failure {
                MoveFailure::NotTurn => println!("Not your turn"),
                MoveFailure::InCheck => println!("You are in check"),
                MoveFailure::CantTakeOwnPieces => println!("You can't take your own pieces"),
                MoveFailure::CantMoveThere => println!("You can't move there"),
            }
            false
        } else {
            // If there were no move failures then we need to move the starting piece
            // to the ending location and clear the starting square
            self.pieces[end_point.row][end_point.col] =
                self.pieces[start_point.row][start_point.col];
            self.pieces[start_point.row][start_point.col] = Piece {
                piece_type: PieceType::Empty,
                color: Color::None,
            };

            // We need to track if the last move was a pawn move two squares forward for en passant
            if self.pieces[end_point.row][end_point.col].piece_type == PieceType::Pawn
                && (start_point.row as i32 - end_point.row as i32).abs() == 2
            {
                self.double_move_pawn = Some(end_point);
            } else {
                self.double_move_pawn = None;
            }

            // When an enpassant occurs there is an extra piece that needs to be removed
            if let Some(point_to_remove) = self.enpassant_piece_to_remove {
                self.pieces[point_to_remove.row][point_to_remove.col] = Piece {
                    piece_type: PieceType::Empty,
                    color: Color::None,
                };
                self.enpassant_piece_to_remove = None;
            }

            self.turn = !self.turn;
            true
        }
    }
}

impl fmt::Display for Board {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let mut string_output: String = String::from("\n");
        for row in 0..8 {
            let mut line = String::from("");
            for col in 0..8 {
                line = line + &self.pieces[7 - row][col].to_string() + " ";
            }
            string_output = string_output + &(8 - row).to_string() + " | " + &line + "\n";
        }
        string_output = string_output + "    -----------------------\n";
        string_output = string_output + "    A  B  C  D  E  F  G  H\n";
        write!(f, "{}", string_output)
    }
}
