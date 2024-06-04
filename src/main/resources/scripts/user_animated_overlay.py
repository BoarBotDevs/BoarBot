from PIL import Image, ImageSequence, ImageFont, ImageDraw, ImageChops
import base64
from io import BytesIO
import requests
import json
import sys

# Inputs from JS

path_config = json.loads(sys.argv[1])
color_config = json.loads(sys.argv[2])
num_config = json.loads(sys.argv[3])
avatar_url = sys.argv[4]
user_tag = sys.argv[5]
score = sys.argv[6]
gifter_avatar_url = sys.argv[7]
gifter_user_tag = sys.argv[8]

image_bytes = sys.stdin.buffer.read()

# Configured directory paths

item_assets = path_config['itemAssets']
other_assets = path_config['otherAssets']
font_assets = path_config['fontAssets']

# Configured asset file paths

circle_mask_path = other_assets + path_config['circleMask']
font_path = font_assets + path_config['mainFont']

# Configured colors

font_color = color_config['font']
bucks_color = color_config['bucks']

# Setting font size from configurations

small_medium_font = num_config['fontSmallMedium']
text_small_medium = ImageFont.truetype(font_path, small_medium_font)

# Setting image positioning and sizes from configurations

avatar_size = (num_config['itemUserAvatarWidth'], num_config['itemUserAvatarWidth'])
user_box_y = num_config['itemBoxOneY']

if gifter_user_tag != '' and gifter_avatar_url != '':
    user_box_y = num_config['itemBoxTwoY']

text_box_extra = num_config['itemTextBoxExtra']
box_x = num_config['itemBoxX']
box_height = num_config['itemBoxHeight']
user_box_extra = num_config['itemUserBoxExtra']

to_pos = (num_config['itemTextX'], (num_config['itemBoxOneY'] + num_config['itemTextYOffset']))
to_box_y = num_config['itemBoxOneY']

from_pos = (num_config['itemTextX'], (num_config['itemBoxThreeY'] + num_config['itemTextYOffset']))
from_box_y = num_config['itemBoxThreeY']

user_avatar_pos = (num_config['itemUserAvatarX'], user_box_y + num_config['itemUserAvatarYOffset'])
user_tag_pos = (num_config['itemUserTagX'], user_box_y + num_config['itemTextYOffset'])

gifter_avatar_pos = (
    num_config['itemUserAvatarX'],
    (num_config['itemBoxFourY'] + num_config['itemUserAvatarYOffset'])
)
gifter_tag_pos = (num_config['itemUserTagX'], (num_config['itemBoxFourY'] + num_config['itemTextYOffset']))
gifter_box_y = num_config['itemBoxFourY']

bucks_pos = (num_config['itemTextX'], (num_config['itemBoxTwoY'] + num_config['itemTextYOffset']))
bucks_box_y = num_config['itemBoxTwoY']

# Opening, converting, and resizing asset files

image = Image.open(BytesIO(image_bytes))

circle_mask = Image.open(circle_mask_path).convert('RGBA').resize(avatar_size)

if gifter_user_tag != '' and gifter_avatar_url != '':
    gifter_avatar = Image.open(BytesIO(requests.get(gifter_avatar_url).content)).convert('RGBA').resize(avatar_size)
    gifter_avatar.putalpha(ImageChops.multiply(gifter_avatar.getchannel('A'), circle_mask.getchannel('A')).convert('L'))

user_avatar = Image.open(BytesIO(requests.get(avatar_url).content)).convert('RGBA').resize(avatar_size)
user_avatar.putalpha(ImageChops.multiply(user_avatar.getchannel('A'), circle_mask.getchannel('A')).convert('L'))

# Stores all newly processed frames
frames = []

# Loops through each animation frame, applying overlays, underlays, and text
for frame in ImageSequence.Iterator(image):
    # Places the nameplate image

    new_frame = frame.copy().convert('RGBA')
    new_frame_draw = ImageDraw.Draw(new_frame)

    if gifter_user_tag != '' and gifter_avatar_url != '':
        new_frame_draw.rounded_rectangle(
            xy=(box_x, to_box_y, box_x+text_small_medium.getlength('To')+text_box_extra, to_box_y+box_height),
            radius=num_config['border'],
            fill=color_config['dark'],
            corners=(False, True, True, False)
        )
        new_frame_draw.text(
            to_pos, 'To', font_color, font=text_small_medium, anchor='ls'
        )

        new_frame_draw.rounded_rectangle(
            xy=(
                box_x, from_box_y, box_x+text_small_medium.getlength('From')+text_box_extra, from_box_y+box_height
            ),
            radius=num_config['border'],
            fill=color_config['dark'],
            corners=(False, True, True, False)
        )
        new_frame_draw.text(
            from_pos, 'From', font_color, font=text_small_medium, anchor='ls'
        )

        new_frame_draw.rounded_rectangle(
            xy=(
                box_x,
                gifter_box_y,
                box_x+text_small_medium.getlength(gifter_user_tag)+user_box_extra,
                gifter_box_y+box_height
            ),
            radius=num_config['border'],
            fill=color_config['dark'],
            corners=(False, True, True, False)
        )
        new_frame_draw.text(
            gifter_tag_pos, gifter_user_tag.encode('utf-16').decode('utf-16'),
            font_color, font=text_small_medium, anchor='ls'
        )

        new_frame.paste(gifter_avatar, gifter_avatar_pos, mask=gifter_avatar)

    new_frame_draw.rounded_rectangle(
        xy=(
            box_x,
            user_box_y,
            box_x+text_small_medium.getlength(user_tag)+user_box_extra,
            user_box_y+box_height
        ),
        radius=num_config['border'],
        fill=color_config['dark'],
        corners=(False, True, True, False)
    )
    new_frame_draw.text(
        user_tag_pos, user_tag.encode('utf-16').decode('utf-16'), font_color, font=text_small_medium, anchor='ls'
    )

    if score != '' and score != '0' and gifter_user_tag == '':
        new_frame_draw.rounded_rectangle(
            xy=(
                box_x,
                bucks_box_y,
                box_x+text_small_medium.getlength('+$' + score)+text_box_extra,
                bucks_box_y+box_height
            ),
            radius=num_config['border'],
            fill=color_config['dark'],
            corners=(False, True, True, False)
        )
        new_frame_draw.text(
            bucks_pos, '+', font_color, font=text_small_medium, anchor='ls'
        )
        new_frame_draw.text(
            (bucks_pos[0] + text_small_medium.getlength('+'), bucks_pos[1]),
            '$' + score, bucks_color, font=text_small_medium, anchor='ls'
        )

    # Places the user avatar image

    new_frame.paste(user_avatar, user_avatar_pos, mask=user_avatar)

    frames.append(new_frame)

# Formatting the result to work with JS

output = BytesIO()
frames[0].save(output, format='GIF', save_all=True, append_images=frames[1:], loop=0, disposal=2)
img_data = output.getvalue()

# Sends the result to JS
print(str(base64.b64encode(img_data))[2:-1])
