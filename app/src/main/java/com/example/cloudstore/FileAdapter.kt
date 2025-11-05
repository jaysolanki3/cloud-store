package com.example.cloudstore

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cloudstore.DataModels.DocumentData
import com.example.cloudstore.databinding.DocumentItemBinding

class FileAdapter(private val context: Context, private val fileList: List<DocumentData>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = DocumentItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileData = fileList[position]

        // Check if the URL is for an image or video
        if (fileData.fileUrl.endsWith(".jpg") || fileData.fileUrl.endsWith(".png") || fileData.fileUrl.endsWith(".jpeg")) {
            // It's an image, load it with Glide
            holder.imageView.visibility = View.VISIBLE
            holder.videoView.visibility = View.GONE
            holder.playIcon.visibility = View.GONE
            Glide.with(context)
                .load(fileData.fileUrl)
                .into(holder.imageView)
        } else if (fileData.fileUrl.endsWith(".mp4") || fileData.fileUrl.endsWith(".avi")) {
            // It's a video, set the VideoView visibility and load the video
            holder.videoView.visibility = View.VISIBLE
            holder.playIcon.visibility = View.VISIBLE
            holder.imageView.visibility = View.GONE
            holder.videoView.setVideoPath(fileData.fileUrl)

            // Set an onClickListener to play the video on tap
            holder.playIcon.setOnClickListener {
                holder.playIcon.visibility = View.GONE
                holder.videoView.start()
                holder.videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                }
            }
        }
        holder.uploaderName.text = "Uploaded by ${fileData.uploadedBy}"

        // You can add more logic to handle other file types or UI updates
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    inner class FileViewHolder(binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.tileImage
        val playIcon: ImageView = binding.playIcon
        val videoView: VideoView = binding.tileVideo
        val uploaderName: TextView = binding.uploaderName
    }
}
