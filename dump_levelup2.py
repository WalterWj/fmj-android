"""Dump all levels for 柳清风's level-up chain, focusing on Lv.35-60."""

with open("/Volumes/Mestor/SoftWare/WorkBuddy/fmj-android/app/src/main/assets/DAT.LIB", "rb") as f:
    data = f.read()

RES_MLR = 12
TYPE_LEVELUP = 2

def get_key(rt, tp, idx):
    return (rt << 16) | (tp << 8) | idx

target_key = get_key(RES_MLR, TYPE_LEVELUP, 1)

found_offset = None
i = 0x10
j = 0x2000
while i < len(data) and data[i] != 0xFF:
    rt = data[i]; tp = data[i+1]; idx = data[i+2] & 0xFF
    key = (rt << 16) | (tp << 8) | idx
    block = data[j] & 0xFF; low = data[j+1] & 0xFF; high = data[j+2] & 0xFF
    value = block * 0x4000 | (high << 8 | low)
    if key == target_key:
        found_offset = value
    i += 3; j += 3

if found_offset is None:
    print("NOT FOUND")
    exit(1)

offset = found_offset
mMaxLevel = data[offset + 2] & 0xFF
level_data_start = offset + 4

print(f"柳清风等级链 maxLv={mMaxLevel}")
print(f"{'Lv':>4} | learnMagic | maxHP | HP | maxMP | MP | atk | def | spd | lingli | luck | nextExp")
print("-" * 90)

for lv in range(1, mMaxLevel + 1):
    base = level_data_start + (lv - 1) * 20
    raw = data[base:base+20]
    maxHP = raw[0] | (raw[1] << 8)
    HP = raw[2] | (raw[3] << 8)
    maxMP = raw[4] | (raw[5] << 8)
    MP = raw[6] | (raw[7] << 8)
    atk = raw[8] | (raw[9] << 8)
    df = raw[10] | (raw[11] << 8)
    # bytes 12-13: unknown
    nextExp = raw[14] | (raw[15] << 8)
    spd = raw[16] & 0xFF
    lingli = raw[17] & 0xFF
    luck = raw[18] & 0xFF
    learn = raw[19] & 0xFF
    flag = " ❌ 异常!" if lv > 35 and learn == 0 else ""
    print(f" {lv:3d} | {learn:10d} | {maxHP:5d} | {HP:3d} | {maxMP:5d} | {MP:3d} | {atk:3d} | {df:3d} | {spd:3d} | {lingli:5d} | {luck:4d} | {nextExp:7d}{flag}")
