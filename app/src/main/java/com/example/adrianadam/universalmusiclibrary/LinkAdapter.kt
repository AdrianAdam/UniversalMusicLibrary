package com.example.adrianadam.universalmusiclibrary

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LinkAdapter(_context: Activity): BaseAdapter() {
    var context: Activity = _context
    var links: ArrayList<Link> = arrayListOf()

    fun addLink(linkAddress: String, key: String) {
        val link = Link()
        link.link = linkAddress
        link.key = key
        links.add(link)
        notifyDataSetChanged()
    }

    fun removeLink(link: Link)
    {
        links.remove(link)

        var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("links/" + FirebaseAuth.getInstance().currentUser?.uid.toString())
        databaseReference.child(link.key).removeValue()

        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val element: View

        val layoutInflater = context.layoutInflater
        element = layoutInflater.inflate(R.layout.list_item, null)
        val link = LinkTag()
        link.link = element.findViewById(R.id.tv_link_element)
        link.remove = element.findViewById(R.id.btn_delete_element)
        element.tag = link

        val tag = element.tag as LinkTag
        tag.link.text = links.get(position).link

        link.remove.setOnClickListener({
            removeLink(links.get(position))
        })

        return element
    }

    override fun getItem(position: Int): Link {
        return links.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return links.size
    }
}