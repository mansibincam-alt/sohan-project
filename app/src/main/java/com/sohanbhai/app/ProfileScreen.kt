package com.sohanbhai.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        // পুরো স্ক্রিনে ঝর্ণার ভিডিও: উপর থেকে নিচ পর্যন্ত
        VideoBackground(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // উপরের বার: বাঁয়ে বাটন, ডানে লোগো
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconCircle(Icons.Filled.ArrowBack, "back", onBack)
                Image(
                    painter = painterResource(R.drawable.sohan_logo),
                    contentDescription = "Sohan Bhai AI logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                // মাঝখানে গোল ছবি, নিচে নাম ও পরিচিতি
                Reveal(0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SohanPhoto(170.dp)
                        Spacer(Modifier.height(16.dp))
                        ShimmerText(SohanProfile.name, 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            SohanProfile.fullName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            SohanProfile.headline,
                            color = Muted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(SohanProfile.location, color = Muted, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Reveal(1) {
                    InfoCard("পরিচয়") {
                        InfoRow(Icons.Filled.MenuBook, Color(0xFF38BDF8), "পরিচয়", SohanProfile.identity)
                        InfoRow(Icons.Filled.Badge, Color(0xFFA78BFA), "পদবি", "${SohanProfile.designation} · ${SohanProfile.area}")
                        InfoRow(Icons.Filled.Groups, Color(0xFF34D399), "সংগঠন", SohanProfile.org)
                        InfoRow(Icons.Filled.Home, Color(0xFFFBBF24), "থাকেন", SohanProfile.location)
                        InfoRow(Icons.Filled.Favorite, Color(0xFFF472B6), "আগ্রহ", SohanProfile.interests.joinToString(" · "))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Reveal(2) {
                    InfoCard("পরিচিতি") {
                        Text(SohanProfile.bio, color = Color.White, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Reveal(3) {
                    InfoCard("যোগাযোগ ও চ্যানেল") {
                        SohanProfile.links.forEach { link ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Night.copy(alpha = 0.6f))
                                    .clickable { openUrl(ctx, link.url) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.OpenInNew, null, tint = Cyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(link.label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Reveal(4) {
                    InfoCard("এই অ্যাপ") {
                        Text(SohanProfile.appNote, color = Color.White, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Reveal(5) {
                    InfoCard("জ্ঞানের বাণী") {
                        SohanProfile.quotes.forEachIndexed { i, q ->
                            if (i > 0) Spacer(Modifier.height(10.dp))
                            Text(q, color = Color(0xFFC4B5FD), fontSize = 14.sp, lineHeight = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun openUrl(ctx: android.content.Context, url: String) {
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** ফেসবুকের মতো সাদাসিধা তথ্য-কার্ড। */
@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    GlowBorder(Modifier.fillMaxWidth(), radius = 18.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/** আইকন (রঙিন গোল-কোণ বাক্সে) + ছোট লেবেল + বড় লেখা। PNG লোগো নয়, Material আইকন। */
@Composable
private fun InfoRow(icon: ImageVector, tint: Color, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}
