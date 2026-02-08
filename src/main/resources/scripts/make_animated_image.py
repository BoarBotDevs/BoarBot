from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

num_config = json.loads(sys.argv[1])
temp_len = json.loads(sys.argv[2])
main_len = json.loads(sys.argv[3])

image_bytes = sys.stdin.buffer.read()

temp_image_bytes = image_bytes[:temp_len]
main_image_bytes = image_bytes[temp_len:temp_len+main_len]

item_size = tuple(num_config['bigBoarSize'])
item_pos = (33, 174)

base_image = Image.open(BytesIO(temp_image_bytes))
main_image = Image.open(BytesIO(main_image_bytes))

frames = []
durations = []

for frame in ImageSequence.Iterator(main_image):
    new_frame = base_image.copy()

    frame = frame.copy().resize(item_size).convert('RGBA')
    new_frame.paste(frame, item_pos)

    new_frame.info['duration'] = frame.info['duration']
    frames.append(new_frame)

output = BytesIO()
frames[0].save(output, format='WEBP', save_all=True, append_images=frames[1:], loop=0)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
