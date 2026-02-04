package com.example.collegeschedule.data.api

import com.example.collegeschedule.data.dto.GroupsDto
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface ScheduleApi {
    @GET("api/schedule/group/{groupName}")
    suspend fun getSchedule(
        @Path("groupName") groupName: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): List<ScheduleByDateDto>
    @GET("api/groups/")
    suspend fun getGroups(
        @Query("course") course: Int?,
        @Query("speciality") speciality: String?
    ): List<GroupsDto>
}