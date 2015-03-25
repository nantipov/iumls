# Overview #

Class **org.palettelabs.iumls.computer.Computer** performs computation of Expression using connected libraries and operators.

# Libraries #

By default 'Computer' has only two libraries connected:
  * "system";
  * "math".


# Operators #

By default 'Computer' has these operators (with precedence) connected:
  * "==" (1) references to library "system";
  * "!=" (1) references to library "system";
  * "||" (2) references to library "system";
  * "&&" (3) references to library "system";
  * ">" (4) references to library "system";
  * "<" (4) references to library "system";
  * ">=" (4) references to library "system";
  * "<=" (4) references to library "system";
  * "+" (5) references to library "math";
  * "-" (5) references to library "math";
  * "`*`" (6) references to library "math";
  * "/" (6) references to library "math".