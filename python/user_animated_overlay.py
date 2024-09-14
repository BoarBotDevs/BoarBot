from PIL import Image, ImageSequence, ImageFont, ImageDraw, ImageChops
import base64
from io import BytesIO
import requests
import json
import sys

base_len = json.loads(sys.argv[1])
overlay_len = json.loads(sys.argv[2])

image_bytes = sys.stdin.buffer.read()

base_image_bytes = image_bytes[:base_len]
overlay_image_bytes = image_bytes[base_len:base_len+overlay_len]

base_image = Image.open(BytesIO(image_bytes))
overlay_image = Image.open(BytesIO(overlay_image_bytes))

overlay_pos = (33, 195)

frames = []

for frame in ImageSequence.Iterator(base_image):
    frame = frame.copy().convert('RGBA')
    frame.paste(overlay_image, overlay_pos, mask=overlay_image)
    frames.append(frame)

output = BytesIO()
frames[0].save(output, format='GIF', save_all=True, append_images=frames[1:], loop=0, disposal=2)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
