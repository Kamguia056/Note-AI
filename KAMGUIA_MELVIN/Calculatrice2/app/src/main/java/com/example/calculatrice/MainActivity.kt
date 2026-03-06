package com.example.calculatrice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatriceApp()
        }
    }
}

@Composable
fun CalculatriceApp() {

    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var justCalculated by remember { mutableStateOf(false) }

    fun calculate() {
        try {
            val tokens = expression.split(" ")

            if (tokens.isEmpty()) return

            var total = tokens[0].toDouble()

            var i = 1
            while (i < tokens.size - 1) {
                val operator = tokens[i]
                val number = tokens[i + 1].toDouble()

                total = when (operator) {
                    "+" -> total + number
                    "-" -> total - number
                    "×" -> total * number
                    "÷" -> total / number
                    else -> total
                }
                i += 2
            }

            result = if (total % 1.0 == 0.0) {
                total.toInt().toString()
            } else {
                total.toString().replace(".", ",")
            }

            justCalculated = true
        } catch (e: Exception) {
            result = "Erreur"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔹 AFFICHAGE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {

            Text(
                text = expression,
                fontSize = 28.sp
            )

            Text(
                text = result,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val buttons = listOf(
            listOf("C","⌫","÷"),
            listOf("7","8","9","×"),
            listOf("4","5","6","-"),
            listOf("1","2","3","+"),
            listOf("0",".","=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {

                                "C" -> {
                                    expression = ""
                                    result = ""
                                }

                                "⌫" -> {
                                    if (expression.isNotEmpty()) {
                                        expression = expression.dropLast(1)
                                    }
                                }

                                "=" -> calculate()

                                "+","-","×","÷" -> {
                                    if (justCalculated) {
                                        expression = result.replace(",", ".")
                                        justCalculated = false
                                    }

                                    if (expression.isNotEmpty() &&
                                        !expression.last().isWhitespace()
                                    ) {
                                        expression += " $label "
                                    }
                                }

                                "." -> {
                                    if (!expression.takeLastWhile { it != ' ' }.contains(".")) {
                                        expression += "."
                                    }
                                }

                                else -> {
                                    if (justCalculated) {
                                        expression = label
                                        result = ""
                                        justCalculated = false
                                    } else {
                                        expression += label
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Text(label, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}