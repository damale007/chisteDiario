package com.conadasoft.chistediario.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.ALARM_SERVICE
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import com.conadasoft.chistediario.Alarma
import com.conadasoft.chistediario.BuildConfig
import com.conadasoft.chistediario.HandlerSQLite
import com.conadasoft.chistediario.R
import com.conadasoft.chistediario.databinding.FragmentChistesBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.time.LocalDateTime

class ChistesFragment : Fragment() {

    private val lanzadorPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted ->
        val mensaje = when {
            isGranted -> ""
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> "Acepta el permiso para recordatorios de chistes nuevos"
            else -> ""
        }

        if (mensaje.isNotEmpty()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    private var _binding: FragmentChistesBinding? = null
    private val binding get() = _binding!!

    private var mInterstitialAd: InterstitialAd? = null
    private var voy = 0
    private var disponibles = 10
    private var dia = 0
    private var mes = 0
    private var ano = 0
    private val frases: Array<String> = arrayOf(
        "Un niño le pregunta a su padre: Papá, ¿qué se siente tener un hijo tan guapo? Y el padre le responde: No sé hijo, pregúntale a tu abuelo.",
        "¿Por qué el libro de matemáticas se deprimió? Porque tenía demasiados problemas.",
        "¿Qué le dice una iguana a su hermana gemela? Somos iguanitas.",
        "¿Cuál es el colmo de Aladdín? Tener mal genio.",
        "¿Qué hace una abeja en el gimnasio? ¡Zum-ba!",
        "¿Por qué el niño lleva una escalera a la escuela? Porque quiere ir a la clase alta.",
        "¿Cómo se despiden los químicos? Ácido un placer.",
        "¿Qué le dice un pez a otro pez? Nada.",
        "¿Cómo se dice pañuelo en japonés? Saka-moko.",
        "¿Por qué los pájaros no usan Facebook? Porque ya tienen Twitter.",
        "¿Cuál es el colmo de un electricista? Perder el hilo de la corriente.",
        "¿Qué le dijo el mar al río? ¡Qué pasa, río!",
        "¿Por qué el libro de historia estaba siempre deprimido? Porque siempre se estaba recordando de los viejos tiempos.",
        "¿Cómo se llama un boomerang que no vuelve? Palo.",
        "¿Por qué los esqueletos no pelean entre ellos? Porque no tienen agallas.",
        "¿Qué le dice un gusano a otro gusano? Voy a dar una vuelta a la manzana.",
        "¿Qué hace un perro con un taladro? Taladra.",
        "¿Cómo hace el perro japonés? ¡Ki-a-dos!",
        "¿Cuál es el café más peligroso del mundo? El ex-preso.",
        "¿Qué le dice una iguana a su hermana gemela? Somos iguanitas.",
        "¿Por qué el niño llevó una escalera a la escuela? Porque quería ir a la clase alta.",
        "¿Cómo se despiden los químicos? Ácido un placer.",
        "¿Qué le dijo un jardinero a otro? ¡Disfrutemos mientras podamos!",
        "¿Por qué las focas miran siempre hacia arriba? Porque ahí están los focos.",
        "¿Qué le dice un semáforo a otro? No me mires que me estoy cambiando.",
        "¿Cuál es el animal más antiguo? La cebra, porque está en blanco y negro.",
        "Dos tomates están cruzando la calle. Uno es atropellado y el otro le grita: ¡Ketchup!",
        "¿Qué le dice una taza a otra taza? ¡Me da un café con leche corto!",
        "Jaimito va a la escuela y la maestra le pregunta: Jaimito, ¿sabes qué planeta va después de Marte? Jaimito responde: No sé, ¿miércoles?",
        "¿Qué está al final de todo? La letra o",
        "¿Dónde cuelga Superman su supercapa? En superchero.",
        "Dos pulgas están en un bar y una le dice a la otra: Oye, ¿te invito a una copa? Y la otra le responde: No, gracias, estoy esperando a mi pulga.",
        "Un hombre entra a una biblioteca, se acerca al bibliotecario y le pregunta: ¿Tiene libros sobre la paranoia? El bibliotecario le responde: ¡Sí, y están justo detrás de ti!",
        "¿Qué le dice un jaguar a otro jaguar? ¡Jaguar you!",
        "¿Qué le dice un techo a otro techo? Nos vemos en la esquina.",
        "¿Qué le dice una pulga a otra pulga? ¡Oye, te invito a una copa! Y la otra le responde: No, gracias, estoy esperando a mi pulga.",
        "¿Cómo se dice tonto en chino? Ching-chang-chong.",
        "¿Qué le dice un árbol a otro árbol? ¡Te echo una rama!",
        "¿Qué le dice un jaguar you a otro jaguar you? ¡Jaguar you too!",
        "¿Qué le dice un bombero a otro bombero? ¡Fuego!",
        "¿Qué le dice un chinche a otro chinche? ¡Chinchemos!",
        "¿Qué le dice un ojo a otro ojo? Entre ceja y ceja.",
        "¿Qué le dice una impresora a otra impresora? ¡Impresionante!",
        "¿Qué le dice un punto a otro punto? ¡Aparte!",
        "¡Soy celíaca!. Encantado, yo Antoniaco",
        "¡Me acaba de picar una serpiente! -¿Cobra? -No, gratis",
        "Mamá ¿las pastillas Juanola vuelan?. -No. -Eentonces me he tragado una mosca",
        "Mamá en el colegio me dicen campana. -¿por qué? -Tolontería de la gente",
        "¿Qué le dice un techo a otro? Techo de menos.",
        "Una madre le dice a su hijo: Jaimito, me ha dicho un pajarito que te drogas. -La que te drogas eres tú, que hablas con pajaritos",
        "¿Cómo se queda un mago después de comer? Magordito.",
        "Mamá, ¿cuándo va a terminar la cuarentena por el covid? -Cállate y cómete tu papel del váter",
        "El profesor le pregunta a Jaimito: Jaimito, ¿qué fórmula química es H2O+CO+CO?. -¡Fácil, profesor! Es agua de coco",
        "¿Qué es un pez en el cine? Pues un mero espectador...",
        "Juan, ¿hiciste al amor con tu esposa antes de casaros? -No, ¿y tú? -Yo sí, pero es que no sabía que era tu novia",
        "¿Cuál es el último animal que subió al arca de Noé? El del-fin.",
        "¿Cómo se dice disparo en árabe? Ahí-va-la-bala.",
        "Si se muere una pulga, ¿a dónde va? Al pulgatorio.",
        "Hijo, me veo gorda, fea y vieja. ¿Qué tengo hijo, qué tengo? Mamá, tienes toda la razón.",
        "¿Cómo se dice pelo sucio en chino? Chin cham pu.",
        "Había una vez un niño tan, tan, tan despistado que... ¡da igual, me he olvidado del chiste!",
        "Le dice la maestra a Jaimito: Jaimito, ¿cómo se dice en inglés el gato se cayó al agua y se ahogó? -Fácil profe, se dice The cat cataplum in the water gluglu no more miau miau",
        "Camarero, ese filete tiene muchos nervios. Pues normal, es la primera vez que se lo comen.",
        "¿Cómo se llama el primo vegetariano de Bruce Lee? Broco Lee.",
        "Sí los zombies se deshacen con el paso del tiempo ¿zombiodegradables?",
        "Cómo se llama el campeón de buceo japonés? Tokofondo.",
        "¿Qué le dice el número 1 al número 10? Para ser como yo, tienes que ser sincero.",
        "¡Niño! echa la primitiva. -Abuela, a la calle.",
        "Soy ateo -Yo anuel, ucho gusto -El gusto es ío.",
        "¿Cómo se dice psicoanalista en japonés? Sakudo Tukoko.",
        "¿Qué tiene Darth Vader en la nevera? Helado Oscuro."
    )
    private var total: Int = 0
    private lateinit var baseDatosSQL: HandlerSQLite
    private var publicaAnuncio = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChistesBinding.inflate(inflater, container, false)

        baseDatosSQL = HandlerSQLite(requireContext())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)

        if (Build.VERSION.SDK_INT >= 33)
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)

        alarma()

        publicidad()

        total = frases.size
        nuevoChiste()

        actualizaContador()
        compruebaLimites()

        eventos()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        alarma()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun eventos() {
        binding.favoritos.setOnClickListener{

            val cadena = baseDatosSQL.select("indice = $voy")
            if (cadena == null) {
                baseDatosSQL.insertFavoritos(voy, frases[voy])
                binding.favoritos.setImageResource(R.drawable.corazonlleno)
                Toast.makeText(requireContext(), "Frase agregada a favoritos", Toast.LENGTH_SHORT).show()
            } else {
                baseDatosSQL.delete("indice = $voy")
                binding.favoritos.setImageResource(R.drawable.corazon)
                Toast.makeText(requireContext(), "Frase eliminada de favoritos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.compartir.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, frases[voy])
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)


            /*val myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val myClip = ClipData.newPlainText("text", frases[voy])
            myClipboard.setPrimaryClip(myClip)
            Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show()*/
        }

        binding.anterior.setOnClickListener {
            if (voy > 0) {
                voy--
                visualizaFrase()
                compruebaLimites()
            }
        }

        binding.siguiente.setOnClickListener {
            if (voy < disponibles) {
                voy++
                visualizaFrase()
                compruebaLimites()
            }
        }
    }

    private fun nuevoChiste() {
        val hoy = LocalDateTime.now()

        dia = hoy.dayOfMonth
        mes = hoy.monthValue
        ano = hoy.year

        val settings = requireContext().getSharedPreferences("Preferencias", MODE_PRIVATE)
        disponibles = settings.getInt("disponible", 10)
        voy = disponibles
        val ultimodia = settings.getInt("dia", dia)
        val ultimomes = settings.getInt("mes", mes)
        val ultimoano = settings.getInt("ano", ano)
        val ultimaFecha = (ultimoano * 365 + (ultimomes * 31) + ultimodia).toFloat()
        val fecha = (ano * 365 + (mes * 31) + dia).toFloat()

        //Nueva cita
        if (fecha > ultimaFecha && disponibles + 1 < total) {
            val dialogo1 = AlertDialog.Builder(requireContext())
            dialogo1.setTitle("Atención")
            dialogo1.setMessage("Hay un nuevo chiste disponible, pero deberá ver un anuncio ¿Deseea desbloquearlo?")
            dialogo1.setPositiveButton("SI") { _: DialogInterface?, _: Int -> muestraFrase()  }
            dialogo1.setNegativeButton("CANCELAR") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            dialogo1.create()
            dialogo1.show()
        }
    }

    private fun publicidad() {
        binding.adView.adUnitId = BuildConfig.UNIT_ID
        val adRequest = AdRequest.Builder().build()

        binding.adView.loadAd(adRequest)

        InterstitialAd.load(
            requireContext(), BuildConfig.ID_ADMOB, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    if (publicaAnuncio) {
                        publicaAnuncio = false
                        mInterstitialAd!!.show(requireActivity())
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("MainActivity", loadAdError.toString())
                    mInterstitialAd = null
                }
            })

    }

    private fun visualizaFrase() {
        binding.frase.text = frases[voy]
        val cadena = "${voy +1}/${disponibles + 1}"
        binding.numero.text = cadena

        val favoritos = baseDatosSQL.select("indice = $voy")
        if (favoritos != null) {
            binding.favoritos.setImageResource(R.drawable.corazonlleno)
        } else {
            binding.favoritos.setImageResource(R.drawable.corazon)
        }
    }

    private fun actualizaContador() {
        visualizaFrase()
        val settings = requireContext().getSharedPreferences("Preferencias", MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("disponible", disponibles)
        editor.putInt("dia", dia)
        editor.putInt("mes", mes)
        editor.putInt("ano", ano)
        editor.apply()
    }

    private fun alarma() {
        val sharedPreferences = requireContext().getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE)
        val tareaProgramada = sharedPreferences.getBoolean("tarea_programada", false)

        if (!tareaProgramada) {
            val am = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager

            val i = Intent(requireContext(), Alarma::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                i,
                PendingIntent.FLAG_IMMUTABLE
            )

            am.cancel(pendingIntent)

            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()

            am.setRepeating(AlarmManager.RTC_WAKEUP,
                cal.timeInMillis + (24*60*60*1000),  24* 60 * 60 * 1000 , pendingIntent)
        }

        val editor = sharedPreferences.edit()
        editor.putBoolean("tarea_programada", true)
        editor.apply()
    }


    private fun compruebaLimites() {
        binding.siguiente.isEnabled = voy < disponibles
        binding.anterior.isEnabled = voy > 0
    }

    private fun muestraFrase() {
        disponibles++
        voy++;
        publicaAnuncio = true
        if (mInterstitialAd != null) {
            publicaAnuncio = false
            mInterstitialAd!!.show(requireActivity())
        }

        actualizaContador()
        compruebaLimites()
        visualizaFrase()
    }
}