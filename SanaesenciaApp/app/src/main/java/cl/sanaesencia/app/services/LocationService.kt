package cl.sanaesencia.app.services
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
class LocationService(context:Context){
    private val client=LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    fun lastLocation(onResult:(android.location.Location?)->Unit){client.lastLocation.addOnSuccessListener(onResult)}
}
