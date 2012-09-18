import os
from subprocess import call

s = "convert $1 -matte -fill none -fuzz 40% -opaque white $2"

ps = [f for f in os.listdir(os.getcwd()) if ".png" in f]

for p in ps:
    call(s.replace("$1", p).replace("$2", p.replace(".png", "-t.png")).split())

