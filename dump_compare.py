"""Compare PC and Android DAT.LIB level-up chain for character 1."""
import struct

def dump_levelup(path, label):
    with open(path, "rb") as f:
        data = f.read()
    
    RES_MLR, TYPE_LEVELUP = 12, 2
    def get_key(rt, tp, idx):
        return (rt << 16) | (tp << 8) | idx
    
    target_key = get_key(RES_MLR, TYPE_LEVELUP, 1)
    found_offset = None
    i = 0x10; j = 0x2000
    while i < len(data) and data[i] != 0xFF:
        rt = data[i]; tp = data[i+1]; idx = data[i+2] & 0xFF
        key = (rt << 16) | (tp << 8) | idx
        block = data[j] & 0xFF; low = data[j+1] & 0xFF; high = data[j+2] & 0xFF
        value = block * 0x4000 | (high << 8 | low)
        if key == target_key:
            found_offset = value
        i += 3; j += 3
    
    if found_offset is None:
        print(f"  [{label}] NOT FOUND")
        return
    
    offset = found_offset
    mMaxLevel = data[offset + 2] & 0xFF
    level_data_start = offset + 4
    
    print(f"\n  [{label}] maxLv={mMaxLevel}")
    print(f"  {'Lv':>4} | learnMagic")
    print(f"  " + "-"*22)
    
    prev = -1
    for lv in range(1, mMaxLevel + 1):
        base = level_data_start + (lv - 1) * 20
        learn = data[base + 19] & 0xFF
        if learn != prev:
            print(f"  {lv:4d} | {learn}")
            prev = learn
    
    # Also show Lv.35-40 for verification
    print(f"\n  Lv.35-40 magic values:")
    for lv in range(35, min(41, mMaxLevel+1)):
        base = level_data_start + (lv - 1) * 20
        learn = data[base + 19] & 0xFF
        print(f"  {lv:4d} | {learn}")

print("=== Level-up chain comparison ===")
dump_levelup("/Volumes/Mestor/SoftWare/WorkBuddy/fmj/assets/DAT.LIB", "PC版")
dump_levelup("/Volumes/Mestor/SoftWare/WorkBuddy/fmj-android/app/src/main/assets/DAT.LIB", "Android版")
