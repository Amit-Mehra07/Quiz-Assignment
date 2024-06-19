package com.example.quizassignment

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.example.quizassignment.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var questions: List<Questions>
    private var currentQuestionIndex: Int = 0
    private var remainingTime: Long = 600000 // 10 minutes in milliseconds
    private lateinit var timer: CountDownTimer
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("QuizApp", Context.MODE_PRIVATE)

        questions = loadQuestions(this)
        loadQuizState()
        setupTimer()
        showQuestion()
        binding.submitAnswerButton.setOnClickListener {
    checkAnswer()
}
    }

    private fun setupTimer() {
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateTimerUI()
            }

            override fun onFinish() {
                endQuiz()
            }
        }
        timer.start()
    }

    private fun updateTimerUI() {
        val minutes = (remainingTime / 1000) / 60
        val seconds = (remainingTime / 1000) % 60
        binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun showQuestion() {
        val question = questions[currentQuestionIndex]
        binding.questionTextView.text = question.question

        val radioGroup = binding.optionsRadioGroup
        radioGroup.removeAllViews()
        for (option in question.options) {
            val radioButton = RadioButton(this)
            radioButton.text = option
            radioGroup.addView(radioButton)
        }
    }

    //here we check answer of the questions
    private fun checkAnswer() {
        val radioGroup = binding.optionsRadioGroup
        val selectedOptionId = radioGroup.checkedRadioButtonId
        if (selectedOptionId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
            val selectedAnswer = selectedRadioButton.text.toString()
            val correctAnswer = questions[currentQuestionIndex].answer

            if (selectedAnswer == correctAnswer) {
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    showQuestion()
                } else {
                    endQuiz()
                }
            } else{
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    showQuestion()
                } else {
                    endQuiz()
                }
            }

        } else {
            Toast.makeText(this, "Please select an option!", Toast.LENGTH_LONG).show()
        }
        saveQuizState()
    }

    private fun endQuiz() {
        timer.cancel()
        Toast.makeText(this, "Quiz Completed!", Toast.LENGTH_LONG).show()
        clearQuizState()
    }

    private fun saveQuizState() {
        val editor = prefs.edit()
        editor.putInt("currentQuestionIndex", currentQuestionIndex)
        editor.putLong("remainingTime", remainingTime)
        editor.apply()
    }

    private fun loadQuizState() {
        currentQuestionIndex = prefs.getInt("currentQuestionIndex", 0)
        remainingTime = prefs.getLong("remainingTime", 600000)
    }

    private fun clearQuizState() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    private fun loadQuestions(context: Context): List<Questions> {
        val inputStream = context.assets.open("questions.json")
        val reader = InputStreamReader(inputStream)
        val quizQuestionsType = object : TypeToken<List<Questions>>() {}.type
        return Gson().fromJson(reader, quizQuestionsType)
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        saveQuizState()
    }

    override fun onResume() {
        super.onResume()
        loadQuizState()
        setupTimer()
    }

}