package com.sohanbhai.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** অ্যাপ প্রথমবার খুললে: "আহলান সাহলান" ওয়েলকাম নোট। "ওকে" ছাড়া বন্ধ হয় না। */
@Composable
fun WelcomeDialog(onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = CardBg,
        title = { ShimmerText("আহলান সাহলান!", 22.sp) },
        text = {
            Text(
                "সোহান ভাইয়ের অ্যাপে আপনাকে স্বাগতম। এখানে পাবেন সোহান ভাইয়ের পরিচয়, এআই চ্যাট, ছবি তৈরি ও এডিট, অনুবাদ এবং PDF পড়ার সুবিধা।",
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onOk) {
                Text("ওকে", color = Cyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/** এআই সেকশনে প্রথমবার ঢুকলে: ছবি ও ইসলামী প্রশ্নের সংক্ষিপ্ত নিয়ম। */
@Composable
fun ChatRulesDialog(onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onOk,
        containerColor = CardBg,
        title = { Text("ব্যবহারের নিয়ম", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("ছবি তৈরি", color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "একবারে সর্বোচ্চ ৫টি ছবি নেওয়া যায়। অর্ডারগুলো প্রতি ১০ সেকেন্ড পর পর তৈরি হয়। ছবি তৈরি চলাকালে নতুন ছবি বানানো যাবে না, তবে এআই-এর সাথে চ্যাট চলবে। অ্যাপ বন্ধ থাকলেও ছবি তৈরি হবে এবং নোটিফিকেশনে আসবে।",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Text("ইসলামী প্রশ্ন", color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "সর্বজনবিদিত ইসলামী বিষয়ে উত্তর দেওয়া হবে। কুরআন-হাদিসের দলিল, ফতোয়া বা জটিল মাসআলার উত্তর দেওয়া হবে না; সেক্ষেত্রে যোগ্য আলেমের কাছে জিজ্ঞেস করুন।",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onOk) {
                Text("ওকে", color = Cyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}
