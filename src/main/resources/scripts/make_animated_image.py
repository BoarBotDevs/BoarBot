from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

path_config = json.loads(sys.argv[1])
num_config = json.loads(sys.argv[2])
main_image_path = sys.argv[3]

temp_bytes = sys.stdin.buffer.read()

image_size = (930, 1080)
item_size = tuple(num_config['bigBoarSize'])
item_pos = (33, 174)

base_image = Image.open(BytesIO(temp_bytes))

main_image = Image.open(main_image_path)

frames = []
durations = []

for frame in ImageSequence.Iterator(main_image):
    new_frame = Image.new('RGBA', image_size)
    new_frame.paste(base_image, (0, 0))

    frame = frame.copy().resize(item_size).convert('RGBA')
    new_frame.paste(frame, item_pos)

    frames.append(new_frame)
    durations.append(frame.info['duration'])

output = BytesIO()
frames[0].save(
    output, format='GIF', save_all=True, append_images=frames[1:], duration=durations, loop=0, disposal=2
)
img_data = output.getvalue()

# Sends the result to JS
print(str(base64.b64encode(img_data))[2:-1])
