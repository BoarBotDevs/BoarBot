from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

# Inputs from JS

path_config = json.loads(sys.argv[1])
num_config = json.loads(sys.argv[2])
main_image_path = sys.argv[3]

temp_bytes = sys.stdin.buffer.read()

# Setting image positioning and sizes from configurations

image_size = tuple(num_config['itemImageSize'])
item_size = tuple(num_config['bigBoarSize'])
item_pos = tuple(num_config['itemPos'])

# Opening, converting, and resizing asset files

base_image = Image.open(BytesIO(temp_bytes))

main_image = Image.open(main_image_path)

# Stores all newly processed frames
frames = []
durations = []

# Loops through each animation frame, applying overlays, underlays, and text
i = 0

for frame in ImageSequence.Iterator(main_image):
    # Places the item image

    new_frame = frame.copy().resize(image_size).convert('RGBA')
    new_frame.paste(base_image, num_config['originPos'])

    new_frame = new_frame.copy().convert('RGBA')
    frame = frame.copy().resize(item_size).convert('RGBA')
    new_frame.paste(frame, item_pos)

    frames.append(new_frame)
    durations.append(frame.info['duration'])

# Formatting the result to work with JS

output = BytesIO()
frames[0].save(
    output, format='GIF', save_all=True, append_images=frames[1:],
    duration=durations, loop=0, disposal=2
)
img_data = output.getvalue()

# Sends the result to JS
print(str(base64.b64encode(img_data))[2:-1])
