package com.martishin.graphsonmaps

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
//        uncomment for load from xml example
//        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
//        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
//        stage.title = "Hello!"
//        stage.scene = scene
//        stage.show()

        val firstNumberField = TextField()
        val secondNumberField = TextField()
        val calculateButton = Button("Calculate")
        val resultLabel = Label()

        calculateButton.setOnAction {
            val firstNumber = firstNumberField.text.toDoubleOrNull() ?: 0.0
            val secondNumber = secondNumberField.text.toDoubleOrNull() ?: 0.0
            val sum = firstNumber + secondNumber
            resultLabel.text = "The sum of $firstNumber and $secondNumber is $sum"
        }

        val vbox = VBox(10.0)
        vbox.padding = Insets(20.0)
        vbox.children.addAll(
            Label("Enter the first number:"),
            firstNumberField,
            Label("Enter the second number:"),
            secondNumberField,
            calculateButton,
            resultLabel,
        )

        val scene = Scene(vbox, 300.0, 250.0)

        stage.title = "Calculator App"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}
