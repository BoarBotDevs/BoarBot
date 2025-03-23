from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

# Inputs from JS

base_len = json.loads(sys.argv[1])
middle_len = json.loads(sys.argv[2])

image_bytes = sys.stdin.buffer.read()

base_image_bytes = image_bytes[:base_len]
middle_image_bytes = image_bytes[base_len:base_len+middle_len]

horiz_padding = 135
image_size = (930, 1080)

base_image = Image.open(BytesIO(base_image_bytes))
middle_image = Image.open(BytesIO(middle_image_bytes))

frames = []
durations = []

for frame in ImageSequence.Iterator(middle_image):
    new_frame = base_image.copy()

    frame = frame.copy()
    new_frame.paste(frame, (horiz_padding, 0))

    new_frame.info['duration'] = frame.info['duration']
    frames.append(new_frame)

output = BytesIO()
frames[0].save(output, format='WEBP', save_all=True, append_images=frames[1:], loop=0, lossless=True)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
