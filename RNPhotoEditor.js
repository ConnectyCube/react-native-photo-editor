import React, { PureComponent } from "react";
import { ViewPropTypes, NativeModules, Platform } from "react-native";
import PropTypes from "prop-types";

const { RNPhotoEditor } = NativeModules;

class PhotoEditor extends PureComponent {
  static propTypes = {
    ...ViewPropTypes,

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
export default function PhotoEditor(props) {
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