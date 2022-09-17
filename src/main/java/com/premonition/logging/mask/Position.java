package com.premonition.logging.mask;

enum Position {
    BEGIN {
        @Override
        String getReplacement(String match, String mask) {
            return mask + match.substring(mask.length());
        }
    },
    END {
        @Override
        String getReplacement(String match, String mask) {
            return match.substring(0, match.length() - mask.length()) + mask;
        }
    };

    abstract String getReplacement(String match, String mask);
}
