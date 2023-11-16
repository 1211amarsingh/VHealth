package com.vhealth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }
    var formatter: SimpleDateFormat = SimpleDateFormat("dd MM yyyy")
    var formatter1 = DateTimeFormatter.ofPattern("dd MM yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        proceedForJob()
    }

    private fun proceedForJob() {

        lifecycleScope.launch {
            readAggregatedStepsLast30Days()

//            readStepsOfLast30DaysAllEntry()
//            readAggregatedCalories()
        }
    }

    private suspend fun readAggregatedStepsLast30Days() {
        val startTime = LocalDateTime.now().minusMonths(1).truncatedTo(ChronoUnit.DAYS)
        val endTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)

        Log.e("Request_Steps", startTime.toString() + "  " + endTime.toString())

        val response =
            healthConnectClient.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Period.ofDays(1)
                )
            )
        try {
            var text = "Last 30 days Step Count Date Wise\n\n "
            for (stepRecord in response) {
                val step = stepRecord.result[StepsRecord.COUNT_TOTAL] ?: 0
                Log.e(TAG, "> " + stepRecord.startTime + " :" + stepRecord.endTime + " :>" + step)
                val date = formatter1.format(stepRecord.startTime)
//            // Process each step record
                text += date + "=> " + step + " Steps \n "
            }
            findViewById<TextView>(R.id.tvInfo).text = text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun readStepsOfLast30DaysAllEntry() {
        val to_time = ZonedDateTime.now()
        val from_time = to_time.minusDays(30)
        Log.e("Request_Steps", from_time.toString() + "  " + to_time.toString())

        val timeRange = TimeRangeFilter.between(
            from_time.toInstant(),
            to_time.toInstant()
        )

        try {
            val response =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = timeRange
                    )
                )
            Log.e("Request_Steps", "Total Record>" + response.records.size)

            var text = "Last 30 days Step Count Date Wise\n\n "
            for (stepRecord in response.records) {
                // Process each step record
                Log.e("Request_Steps", ">" + stepRecord.count)
                text += formatter.format(Date.from(stepRecord.startTime)) + "=> " + stepRecord.count + " Steps\n "
            }
            findViewById<TextView>(R.id.tvInfo).text = text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun readAggregatedCalories() {
        val today = ZonedDateTime.now()
        val startOfDayOfThisMonth = today.withDayOfMonth(1)
            .truncatedTo(ChronoUnit.DAYS)
        val elapsedDaysInMonth = Duration.between(startOfDayOfThisMonth, today)
            .toDays() + 1
        Log.e(
            TAG,
            "readAggregatedData: ${startOfDayOfThisMonth.toString()}  ${today.toString()}   ${elapsedDaysInMonth.toString()}"
        )

        val timeRangeFilter = TimeRangeFilter.between(
            startOfDayOfThisMonth.toInstant(),
            today.toInstant()
        )

        val data = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    TotalCaloriesBurnedRecord.ENERGY_TOTAL
                ),
                timeRangeFilter = timeRangeFilter,
            )
        )

        Log.e(TAG, "readAggregatedData: stepsTotal${Gson().toJson(data)}")
        val steps = data[StepsRecord.COUNT_TOTAL] ?: 0
        val averageSteps = steps / elapsedDaysInMonth
        Log.e(TAG, "readAggregatedData: averageSteps$averageSteps")
    }

    fun addSampleRecord(view: View) {
        val today_00 = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Calcutta"))
        val today_23_59 = LocalDate.now().atTime(23, 59, 0).atZone(ZoneId.of("Asia/Calcutta"))

        Log.e(TAG, "addSampleRecord:Start $today_00  == $today_23_59")

        val records = arrayListOf<Record>()

        for (i in 1..100) {
            val startTime = today_00.minusDays(i.toLong()).toInstant()
            val endTime = today_23_59.minusDays(i.toLong()).toInstant()
            val steps = generateRandom()
            Log.e(TAG, "addSampleRecord:Current $startTime  == $endTime   $steps")

            records.add(
                StepsRecord(
                    count = steps.toLong(),
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = null,
                    endZoneOffset = null,
                )
            )
        }
        //TotalCaloriesBurnedRecord(
        //                energy = Energy.calories(caloriesBurned),
        //                startTime = startTime,
        //                endTime = endTime,
        //                startZoneOffset = null,
        //                endZoneOffset = null,
        //            )

        lifecycleScope.launch {
            healthConnectClient.insertRecords(records)
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Records inserted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun generateRandom(): Int {
        val randomPin = (Math.random() * 9000).toInt() + 1000
        return randomPin
    }
}