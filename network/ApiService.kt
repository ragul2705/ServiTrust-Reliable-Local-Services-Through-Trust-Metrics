package com.servitrust.app.network

import com.servitrust.app.model.AuthResponse
import com.servitrust.app.model.LoginRequest
import com.servitrust.app.model.ProviderLoginRequest
import com.servitrust.app.model.ProviderRegisterRequest
import com.servitrust.app.model.RegisterRequest
import com.servitrust.app.model.ReviewRequest
import com.servitrust.app.model.ServiceProvider
import com.servitrust.app.model.ServiceRequest
import com.servitrust.app.model.TrustHistoryItem
import com.servitrust.app.model.TrustScoreResponse
import com.servitrust.app.model.User
import com.servitrust.app.model.ComplaintRequest
import com.servitrust.app.model.ComplaintResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): User

    @POST("api/users")
    suspend fun register(@Body request: RegisterRequest): User

    @POST("api/providers-auth/register")
    suspend fun providerRegister(@Body req: ProviderRegisterRequest): AuthResponse

    @POST("api/providers-auth/login")
    suspend fun providerLogin(@Body req: ProviderLoginRequest): AuthResponse

    @GET("api/providers")
    suspend fun getAllProviders(): List<ServiceProvider>

    @GET("api/providers/{providerId}")
    suspend fun getProviderById(@Path("providerId") providerId: Long): ServiceProvider

    @GET("api/providers/{providerId}/trust-score")
    suspend fun getProviderTrustScore(@Path("providerId") providerId: Long): TrustScoreResponse

    @GET("api/providers/{providerId}/trust-history")
    suspend fun getProviderTrustHistory(@Path("providerId") providerId: Long): List<TrustHistoryItem>

    @POST("api/requests")
    suspend fun createRequest(@Body request: ServiceRequest): ServiceRequest

    @GET("api/requests/provider/{providerId}")
    suspend fun getRequestsByProvider(@Path("providerId") providerId: Long): List<ServiceRequest>

    @GET("api/requests/user/{userId}")
    suspend fun getRequestsByUser(@Path("userId") userId: Long): List<ServiceRequest>

    @PUT("api/requests/{requestId}/accept")
    suspend fun acceptRequest(@Path("requestId") requestId: Long): ServiceRequest

    @PUT("api/requests/{requestId}/complete")
    suspend fun completeRequest(@Path("requestId") requestId: Long): ServiceRequest

    @PUT("api/requests/{requestId}/cancel-provider")
    suspend fun cancelRequestProvider(@Path("requestId") requestId: Long): ServiceRequest

    @PUT("api/requests/{requestId}/cancel-user")
    suspend fun cancelRequestUser(@Path("requestId") requestId: Long): ServiceRequest

    @POST("api/requests/{requestId}/review/{userId}")
    suspend fun submitReview(
        @Path("requestId") requestId: Long,
        @Path("userId") userId: Long,
        @Body review: ReviewRequest
    ): ServiceRequest

    @POST("api/providers/{providerId}/recompute-trust")
    suspend fun recomputeTrust(
        @Path("providerId") providerId: Long,
        @Query("reason") reason: String = "Manual refresh from app"
    ): TrustScoreResponse

    @POST("api/complaints")
    suspend fun createComplaint(@Body req: ComplaintRequest): ComplaintResponse

    @GET("api/providers/ranking")
    suspend fun getProviderRanking(): List<TrustScoreResponse>

    @GET("api/complaints/pending")
    suspend fun adminPendingComplaints(): List<ComplaintResponse>

    @PUT("api/complaints/{complaintId}/verify")
    suspend fun adminVerifyComplaint(@Path("complaintId") complaintId: Long): ComplaintResponse

    @PUT("api/complaints/{complaintId}/reject")
    suspend fun adminRejectComplaint(@Path("complaintId") complaintId: Long): ComplaintResponse

    @GET("api/admin/complaints/pending")
    suspend fun getPendingComplaints(): List<ComplaintResponse>

    @PUT("api/admin/complaints/{id}/verify")
    suspend fun verifyComplaint(@Path("id") id: Long): ComplaintResponse

    @PUT("api/admin/complaints/{id}/reject")
    suspend fun rejectComplaint(@Path("id") id: Long): ComplaintResponse

    @GET("api/admin/users")
    suspend fun adminGetUsers(): List<User>

    @PUT("api/admin/users/{id}/verify")
    suspend fun adminVerifyUser(@Path("id") id: Long): User

    @GET("api/admin/providers")
    suspend fun adminGetProviders(): List<ServiceProvider>

    @PUT("api/admin/providers/{id}/verify")
    suspend fun adminVerifyProvider(@Path("id") id: Long): ServiceProvider

    @GET("/api/providers/search")
    suspend fun searchProvidersByServiceType(
        @Query("serviceType") serviceType: String
    ): List<ServiceProvider>
}