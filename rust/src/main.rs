mod board;
mod piece;

use text_io;

fn parse_input() -> board::Point {
    let mut col: usize = 8;
    let mut row: usize = 8;
    while col > 7 && row > 7 {
        println!("Enter your move :");
        let word: String = text_io::read!();
        col = match word.chars().nth(0).unwrap() {
            'A' => 0,
            'B' => 1,
            'C' => 2,
            'D' => 3,
            'E' => 4,
            'F' => 5,
            'G' => 6,
            'H' => 7,
            _ => 8,
        };
        row = word.chars().nth(1).unwrap() as usize - '0' as usize - 1;
    }
    board::Point { col, row }
}

fn get_move() -> (board::Point, board::Point) {
    (parse_input(), parse_input())
}

fn main() {
    let mut board = board::Board::new();

    loop {
        println!("{}", board);
        let (start_point, end_point) = get_move();
        println!("{}, {}", start_point, end_point);
        board.make_move(start_point, end_point);
    }
}
