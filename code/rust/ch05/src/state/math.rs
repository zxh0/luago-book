// a % b == a - ((a // b) * b)
pub fn i_mod(a: i64, b: i64) -> i64 {
    a - i_floor_div(a, b) * b
}

// a % b == a - ((a // b) * b)
pub fn f_mod(a: f64, b: f64) -> f64 {
    if a > 0.0 && is_positive_infinite(b) || a < 0.0 && is_negative_infinite(b) {
        a
    } else if a > 0.0 && is_negative_infinite(b) || a < 0.0 && is_positive_infinite(b) {
        b
    } else {
        a - (a / b).floor() * b
    }
}

pub fn i_floor_div(a: i64, b: i64) -> i64 {
    if a > 0 && b > 0 || a < 0 && b < 0 || a % b == 0 {
        a / b
    } else {
        a / b - 1
    }
}

pub fn f_floor_div(a: f64, b: f64) -> f64 {
    (a / b).floor()
}

pub fn shift_left(a: i64, n: i64) -> i64 {
    if n >= 64 {
        0
    } else if n >= 0 {
        a << n
    } else {
        shift_right(a, -n)
    }
}

// logical shift right
pub fn shift_right(a: i64, n: i64) -> i64 {
    if n >= 64 {
        0
    } else if n >= 0 {
        (a as u64 >> n) as i64
    } else {
        shift_left(a, -n)
    }
}

fn is_positive_infinite(n: f64) -> bool {
    n.is_infinite() && n.is_sign_positive()
}

fn is_negative_infinite(n: f64) -> bool {
    n.is_infinite() && n.is_sign_negative()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn modulo() {
        assert_eq!(i_mod(5, 3), 2);
        assert_eq!(i_mod(-5, 3), 1);
        assert_eq!(f_mod(5.0, -3.0), -1.0);
        assert_eq!(f_mod(-5.0, -3.0), -2.0);
    }

    #[test]
    fn floor_div() {
        assert_eq!(i_floor_div(5, 3), 1);
        assert_eq!(i_floor_div(-5, 3), -2);
        assert_eq!(f_floor_div(5.0, -3.0), -2.0);
        assert_eq!(f_floor_div(-5.0, -3.0), 1.0);
    }

    #[test]
    fn shift() {
        assert_eq!(shift_left(0xFF, 100), 0);
        assert_eq!(shift_left(0xFF, -4), 0x0F);
        assert_eq!(shift_right(0xFF, 100), 0);
        assert_eq!(shift_right(0xFF, -4), 0xFF0);
    }
}
