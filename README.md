# project-native-app

### Prepare project
- clone project to local
```shell
git clone git@github.com:ku-final-project/project-native-app.git
```

- open with android studio

### File structure
```
app/src/main/java/com/example/myapplication/
├── ChangePinPage.kt
├── ConfigPage.kt
├── FaceDetectionPage.kt
├── PinPage.kt
├── api
│   └── ApiService.kt
├── camera
│   ├── BaseImageAnalyzer.kt
│   ├── CameraManager.kt
│   └── GraphicOverlay.kt
├── face_detection
│   ├── FaceContourDetectionProcessor.kt
│   └── FaceContourGraphic.kt
└── usb
    └── Usb.kt
```

```
app/src/main/res
├── drawable
│         ├── arrow_down.png
│         ├── arrow_left.png
│   ├── arrow_right.png
│   ├── arrow_up.png
│   └── ku_logo.png
├── layout
│   ├── change_pin_page.xml
│   ├── config_page.xml
│   ├── face_detection_page.xml
│   └── pin_page.xml
├── raw
│   ├── correct_sound_effect.mp3
│   └── wrong_sound_effect.mp3
├── values
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
```

##### ChangePinPage.kt
- Application activity that use in change_pin_page layout.

##### ConfigPage.kt
- Application activity that use in config_page layout.

##### FaceDetectionPage.kt
- Application activity that use in face_detection_page layout.

##### PinPage.kt
- Application activity that use in pin_page layout.

##### api/ApiService.kt
- Use to create HTTP request to server such as open door, face recognition.

##### camera
- Use to set camera manager, crop image, draw rectangle when detect face.

##### face_detection
- Use to calculate spoofing logic and send that face to face recognition API.

##### usb/Usg.kt
- Use to send usb serial to hardware(in optional).

##### drawable
- Use to store image assets.

##### layout
- Layout for page in application.

##### raw
- Use to store sound assets.

##### values
- Use to set constant value that use in application such as colors, words.
