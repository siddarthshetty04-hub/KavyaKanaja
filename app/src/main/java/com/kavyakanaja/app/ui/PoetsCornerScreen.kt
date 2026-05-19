package com.kavyakanaja.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavyakanaja.app.R

data class Poet(
    val name: String,
    val period: String,
    val awards: String,
    val description: String
)

val samplePoets = listOf(
    Poet(
        "Kuvempu",
        "1904 – 1994",
        "Jnanpith Award (1967)",
        "Kuppali Venkatappa Puttappa, popularly known by his pen name Kuvempu, was an Indian poet, playwright, novelist and critic. He is widely regarded as the greatest Kannada poet of the 20th century."
    ),
    Poet(
        "D. R. Bendre",
        "1896 – 1981",
        "Jnanpith Award (1973)",
        "Dattatreya Ramachandra Bendre was an Indian poet of the Kannada language. He was generally considered the greatest Kannada lyric poet of the 20th century."
    ),
    Poet(
        "D. V. Gundappa",
        "1887 – 1975",
        "Padma Bhushan (1974)",
        "Devanahalli Venkataramanaiah Gundappa, popularly known as DVG, was a Kannada writer, poet, and philosopher. His most notable work is the Mankuthimmana Kagga."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoetsCornerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.poets_corner),
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.ivory)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.deep_teal)
                )
            )
        },
        containerColor = colorResource(id = R.color.ivory)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(samplePoets) { poet ->
                PoetCard(poet = poet)
            }
        }
    }
}

@Composable
fun PoetCard(poet: Poet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.warm_cream)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for Poet Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.saffron)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = poet.name.take(1),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = poet.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.dark_walnut)
                    )
                    Text(
                        text = poet.period,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.rich_brown)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🏆 ${poet.awards}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.deep_teal),
                modifier = Modifier
                    .background(colorResource(id = R.color.parchment), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = poet.description,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = colorResource(id = R.color.charcoal)
            )
        }
    }
}
