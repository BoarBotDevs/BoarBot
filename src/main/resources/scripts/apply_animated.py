from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

num_config = json.loads(sys.argv[1])
overlay_pos = tuple(json.loads(sys.argv[2]))
overlay_size = tuple(json.loads(sys.argv[3]))
base_len = json.loads(sys.argv[4])
overlay_len = json.loads(sys.argv[5])

image_bytes = sys.stdin.buffer.read()
base_image_bytes = image_bytes[:base_len]
overlay_image_bytes = image_bytes[base_len:base_len+overlay_len]

base_image = Image.open(BytesIO(base_image_bytes)).convert('RGBA')
overlay_image = Image.open(BytesIO(overlay_image_bytes))

frames = []
durations = []

for frame in ImageSequence.Iterator(overlay_image):
    new_frame = base_image.copy()

    frame = frame.copy().resize(overlay_size).convert('RGBA')
    new_frame.paste(frame, overlay_pos, mask=frame)

    new_frame.info['duration'] = frame.info['duration']
    frames.append(new_frame)

output = BytesIO()
frames[0].save(output, format='WEBP', save_all=True, append_images=frames[1:], loop=0, lossless=True)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
