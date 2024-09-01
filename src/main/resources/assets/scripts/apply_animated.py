from PIL import Image, ImageSequence
import base64
from io import BytesIO
import json
import sys

num_config = json.loads(sys.argv[1])
overlay_path = sys.argv[2]
overlay_pos = tuple(json.loads(sys.argv[3]))
overlay_size = tuple(json.loads(sys.argv[4]))

base_bytes = sys.stdin.buffer.read()

overlay_image = Image.open(overlay_path)
base_image = Image.open(BytesIO(base_bytes)).convert('RGBA')

base_image_size = (base_image.width, base_image.height)

frames = []
durations = []

for frame in ImageSequence.Iterator(overlay_image):
    new_frame = Image.new('RGBA', base_image_size)
    new_frame.paste(base_image, (0, 0))

    frame = frame.copy().resize(overlay_size).convert('RGBA')
    new_frame.paste(frame, overlay_pos, mask=frame)

    frames.append(new_frame)
    durations.append(frame.info['duration'])

output = BytesIO()
frames[0].save(
    output, format='GIF', save_all=True, append_images=frames[1:], duration=durations, loop=0, disposal=2
)
img_data = output.getvalue()

print(str(base64.b64encode(img_data))[2:-1])
