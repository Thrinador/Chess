mod board;

fn main() {
    let board = board::Board::new();

    println!("{}", board);
    let mut line = String::new();
    println!("Enter your move :");
    std::io::stdin().read_line(&mut line).unwrap();
    println!("{}", line);
}
