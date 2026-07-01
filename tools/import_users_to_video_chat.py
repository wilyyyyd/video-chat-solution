import time
import hmac
import hashlib
import base64
import json
import zlib
import random
import requests

SDKAPPID = 0
SECRETKEY = ""
EXPIRETIME = 604800
IDENTIFIER = "administrator"

BASE_URL = "https://console.tim.qq.com/v4/im_open_login_svc/multiaccount_import"
PROFILE_URL = "https://console.tim.qq.com/v4/profile/portrait_set"

# ── User Data ─────────────────────────────────────────────────────────────────
USERS = [
    {
        "UserID": "VideoChatlinxiaoyu",
        "Nick": "Aria",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_7.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Chasing sunsets and cloud-nine moments 🌅✨",
        "Age": 23,
    },
    {
        "UserID": "VideoChatsu_menghan",
        "Nick": "Scarlett",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_13.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Dance like nobody's watching, love like you've never been hurt 💃🖤",
        "Age": 21,
    },
    {
        "UserID": "VideoChatchenkexin",
        "Nick": "Luna",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_14.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Wandering through art galleries and rainy cobblestone streets 🎨☔",
        "Age": 25,
    },
    {
        "UserID": "VideoChatzhaoxinyi",
        "Nick": "Zoe",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_17.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Fashion lover & skincare addict. Living my best life, one outfit at a time 💄👗",
        "Age": 22,
    },
    {
        "UserID": "VideoChatjiangsiqi",
        "Nick": "Ivy",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_19.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Yoga at sunrise, photography at golden hour. Let's make memories together 📷🧘‍♀️",
        "Age": 24,
    },
    {
        "UserID": "VideoChatzhouyaqi",
        "Nick": "Stella",
        "FaceUrl": "https://im.sdk.qcloud.com/download/tuikit-resource/avatar/avatar_21.png",
        "Gender": "Gender_Type_Female",
        "Signature": "Headphones on, latte in hand. Looking for my favorite duet ☕️🎵",
        "Age": 23,
    },
]


# ── UserSig Generation ───────────────────────────────────────────────────────
def gen_user_sig(identifier: str) -> str:
    current = int(time.time())
    obj = {
        "TLS.ver": "2.0",
        "TLS.identifier": identifier,
        "TLS.sdkappid": SDKAPPID,
        "TLS.expire": EXPIRETIME,
        "TLS.time": current,
    }
    key_order = [
        "TLS.identifier",
        "TLS.sdkappid",
        "TLS.time",
        "TLS.expire",
    ]
    string_to_sign = ""
    for key in key_order:
        string_to_sign += f"{key}:{obj[key]}\n"

    sig = hmac_sha256(string_to_sign)
    obj["TLS.sig"] = sig

    json_data = json.dumps(obj, separators=(',', ':'), sort_keys=True).encode('utf-8')
    compressed = zlib.compress(json_data, level=zlib.Z_BEST_SPEED)
    return base64_url_encode(compressed)


def hmac_sha256(plain_text: str) -> str:
    secret_key = SECRETKEY.encode('ascii')
    message = plain_text.encode('ascii')
    signature = hmac.new(secret_key, message, digestmod=hashlib.sha256).digest()
    return base64.b64encode(signature).decode('utf-8')


def base64_url_encode(data: bytes) -> str:
    base64_str = base64.b64encode(data).decode('utf-8')
    return base64_str.replace('+', '*').replace('/', '-').replace('=', '_')


# ── Batch Import Users ───────────────────────────────────────────────────────
def import_users(users: list) -> dict:
    user_sig = gen_user_sig(IDENTIFIER)
    rand = random.randint(0, 4294967295)

    url = (
        f"{BASE_URL}"
        f"?sdkappid={SDKAPPID}"
        f"&identifier={IDENTIFIER}"
        f"&usersig={user_sig}"
        f"&random={rand}"
        f"&contenttype=json"
    )

    payload = {"AccountList": users}
    response = requests.post(url, json=payload, timeout=10)
    return response.json()


# ── Set User Profiles ─────────────────────────────────────────────────────────
def build_profile_items(user: dict) -> list:
    """Build ProfileItem array required by portrait_set API."""
    items = []

    # Standard profile fields
    def add(tag: str, value):
        if value:
            items.append({"Tag": tag, "Value": value})

    add("Tag_Profile_IM_Nick", user.get("Nick", ""))
    add("Tag_Profile_IM_Gender", user.get("Gender", ""))
    add("Tag_Profile_IM_SelfSignature", user.get("Signature", ""))

    # Tag_Profile_IM_BirthDay expects uint32, use age directly
    age = user.get("Age", 0)
    if age > 0:
        items.append({"Tag": "Tag_Profile_IM_BirthDay", "Value": age})

    return items


def set_portrait(user_id: str, profile_items: list) -> dict:
    """Call profile/portrait_set to set a single user's profile."""
    user_sig = gen_user_sig(IDENTIFIER)
    rand = random.randint(0, 4294967295)

    url = (
        f"{PROFILE_URL}"
        f"?sdkappid={SDKAPPID}"
        f"&identifier={IDENTIFIER}"
        f"&usersig={user_sig}"
        f"&random={rand}"
        f"&contenttype=json"
    )

    payload = {
        "From_Account": user_id,
        "ProfileItem": profile_items,
    }
    response = requests.post(url, json=payload, timeout=10)
    return response.json()


# ── Main ──────────────────────────────────────────────────────────────────────


def main():
    print(f"Importing {len(USERS)} users...")
    print(f"SDKAppID: {SDKAPPID}")
    print(f"Identifier: {IDENTIFIER}")
    print("=" * 60)

    # 1. Batch import users
    result = import_users(USERS)

    if result.get("ErrorCode") != 0:
        print(f"\n❌ Import failed: ErrorCode={result.get('ErrorCode')}, ErrorInfo={result.get('ErrorInfo')}")
        return

    fail_accounts = result.get("FailAccounts", [])
    if fail_accounts:
        print(f"\nWarning: Some imports failed: {fail_accounts}")

    print(f"\n Import complete! Setting user profiles...")
    print("-" * 60)

    # 2. Set profiles one by one
    for user in USERS:
        user_id = user["UserID"]
        nick = user["Nick"]
        profile_items = build_profile_items(user)

        print(f"  Setting [{user_id}] ({nick}) ... ", end="", flush=True)
        result = set_portrait(user_id, profile_items)

        if result.get("ErrorCode") == 0:
            print("success")
        else:
            print(f"failel: ErrorCode={result.get('ErrorCode')}, ErrorInfo={result.get('ErrorInfo')}")

    print("\n" + "=" * 60)
    print("User ID list (for MainActivity):")
    for user in USERS:
        print(f'  "{user["UserID"]}",')


if __name__ == "__main__":
    main()
