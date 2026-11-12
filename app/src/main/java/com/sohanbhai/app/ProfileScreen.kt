package com.sohanbhai.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        StarField()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // কভার: প্রাকৃতিক দৃশ্য, বাঁয়ে বাটন, ডানে লোগো
            Box(Modifier.fillMaxWidth().height(190.dp)) {
                VideoBackground(Modifier.fillMaxSize())
                Box(Modifier.statusBarsPadding().padding(12.dp)) {
                    IconCircle(Icons.Filled.ArrowBack, "back", onBack)
                }
                Image(
                    painter = painterResource(R.drawable.sohan_logo),
                    contentDescription = "Sohan Bhai AI logo",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Column(Modifier.padding(horizontal = 18.dp)) {
                // ছবিটি কভারের উপরে ভেসে থাকে; লেআউটে জায়গা নেয় ৫৪ dp
                Box(Modifier.fillMaxWidth().height(54.dp)) {
                    SohanPhoto(104.dp, modifier = Modifier.offset(y = (-50).dp))
                }

                Reveal(0) {
                    Column {
                        ShimmerText(SohanProfile.name, 26.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(SohanProfile.fullName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(SohanProfile.headline, color = Muted, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(SohanProfile.location, color = Muted, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Reveal(1) {
                    InfoCard("পরিচয়") {
                        InfoRow(Icons.Filled.Person, "পরিচয়", SohanProfile.identity)
                        InfoRow(Icons.Filled.People, "পদবি", "${SohanProfile.designation} · ${SohanProfile.area}")
                        InfoRow(Icons.Filled.People, "সংগঠন", SohanProfile.org)
                        InfoRow(Icons.Filled.LocationOn, "থাকেন", SohanProfile.location)
                        InfoRow(Icons.Filled.Favorite, "আগ্রহ", SohanProfile.interests.joinToString(" · "))
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

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Muted, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 14.sp)
        }
    }
}
