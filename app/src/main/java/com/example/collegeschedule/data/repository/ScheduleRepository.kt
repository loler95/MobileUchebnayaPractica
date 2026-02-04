package com.example.collegeschedule.data.repository
import com.example.collegeschedule.data.api.ScheduleApi
import com.example.collegeschedule.data.dto.GroupsDto
import com.example.collegeschedule.data.dto.ScheduleByDateDto

class ScheduleRepository(private val api: ScheduleApi) {
    suspend fun loadSchedule(
        groupName: String,
        start: String,
        end: String
    ): List<ScheduleByDateDto> {
        return api.getSchedule(groupName, start, end)
    }
    suspend fun getGroups(
        course: Int? = null,
        speciality: String? = null
    ): List<GroupsDto> {
        return api.getGroups(course = course, speciality = speciality)
    }
}