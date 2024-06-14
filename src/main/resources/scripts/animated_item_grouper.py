from PIL import Image, ImageSequence, ImageFont, ImageDraw, ImageChops
import base64
from io import BytesIO
import requests
import json
import sys
import math

# Inputs from JS

num_config = json.loads(sys.argv[1]) // TODO: Fix
base_len = json.loads(sys.argv[2])
middle_len = json.loads(sys.argv[3])

image_bytes = sys.stdin.buffer.read()

base_image_bytes = image_bytes[:base_len]
middle_image_bytes = image_bytes[base_len:base_len+middle_len]

horiz_padding = num_config['itemHorizPadding']

image_size = tuple(num_config['itemImageSize'])

base_image = Image.open(BytesIO(base_image_bytes))
middle_image = Image.open(BytesIO(middle_image_bytes))

frames = []
durations = []

for frame in ImageSequence.Iterator(middle_image):
    new_frame = base_image.copy()

    frame = frame.copy()
    new_frame.paste(frame, (horiz_padding, 0))

    frames.append(new_frame)
    durations.append(frame.info['duration'])

output = BytesIO()
frames[0].save(output, format='GIF', save_all=True, append_images=frames[1:], loop=0, disposal=2, duration=durations)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
