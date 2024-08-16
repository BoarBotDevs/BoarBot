from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

num_config = json.loads(sys.argv[1])
item_path = sys.argv[2]
base_len = json.loads(sys.argv[3])
border_len = json.loads(sys.argv[4])

image_bytes = sys.stdin.buffer.read()

base_bytes = image_bytes[:base_len]
border_bytes = image_bytes[base_len:base_len+border_len]

image = Image.open(item_path)
base_image = Image.open(BytesIO(base_bytes)).convert('RGBA')
border_image = Image.open(BytesIO(border_bytes)).convert('RGBA')

image_size = (1920, 1403)

item_pos = (1108, 455)
item_size = tuple(num_config['mediumBigBoarSize'])

frames = []
durations = []

for frame in ImageSequence.Iterator(image):
    new_frame = Image.new('RGBA', image_size)
    new_frame.paste(base_image, (0, 0))

    frame = frame.copy().resize(item_size).convert('RGBA')
    new_frame.paste(frame, item_pos)

    new_frame.paste(border_image, item_pos, mask=border_image)

    frames.append(new_frame)
    durations.append(frame.info['duration'])

output = BytesIO()
frames[0].save(
    output, format='GIF', save_all=True, append_images=frames[1:], duration=durations, loop=0, disposal=2
)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
