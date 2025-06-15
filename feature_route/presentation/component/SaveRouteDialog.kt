package com.polygonbikes.ebike.v3.feature_route.presentation.component

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun SaveRouteDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var routeName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = stringResource(id = R.string.save_route_dialog_prompt))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text(stringResource(id = R.string.save_route_dialog_hint)) }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.label_button_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (routeName.isNotBlank()) {
                        onConfirm(routeName)
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.label_button_save),
                    color = colorResource(id = R.color.text),
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Preview(showBackground = false)
@Composable
fun PreviewSaveRouteDialog() {
    SaveRouteDialog(
        title = "Save Route",
        onDismiss = {},
        onConfirm = {
            routeName -> Log.d("SaveRoute",
            "Route Name: $routeName")
        }
    )
}
