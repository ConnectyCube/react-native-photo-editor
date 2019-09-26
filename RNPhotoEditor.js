import { NativeModules } from 'react-native'

const { RNPhotoEditor } = NativeModules

const defaultColors = [
    '#000000',
    '#808080',
    '#a9a9a9',
    '#FFFFFF',
    '#0000ff',
    '#00ff00',
    '#ff0000',
    '#ffff00',
    '#ffa500',
    '#800080',
    '#00ffff',
    '#a52a2a',
    '#ff00ff'
]

export default function PhotoEditor(props) {
    const {
        colors = defaultColors,
        editedImageDirectory = null,
        colorPrimary = null,
        hiddenControls = [],
        onCancel = () => {},
        onDone = () => {},
        path = null,
        stickers = []
    } = props
    RNPhotoEditor.Edit(
        {
            colors,
            hiddenControls,
            onCancel,
            editedImageDirectory,
            colorPrimary,
            onDone,
            path,
            stickers
        },
        onDone,
        onCancel
    )
}

export { PhotoEditor as RNPhotoEditor }
