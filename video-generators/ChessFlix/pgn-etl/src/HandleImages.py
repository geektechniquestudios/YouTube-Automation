import cairosvg
import imageio
from chess import svg

"""
    Please note that part of this code is borrowed from pgn2gif
    https://github.com/mzfr/pgn2gif
"""


def position_to_image(board, size, color):
    """
        Produce an image for every move

        :board: object for the board moves
        :size: size of the board
        :color: color of the chess board
    """
    css = {'green': 'css/green.css', 'blue': 'css/blue.css'}
    css_content = None
    if color != 'brown':
        css_content = open(css[color]).read()
    svg_file = svg.board(board, size=size, style=css_content)
    __bytes = cairosvg.svg2png(bytestring=svg_file)

    return imageio.imread(__bytes, format="PNG")  # pilmode="RGBA",




