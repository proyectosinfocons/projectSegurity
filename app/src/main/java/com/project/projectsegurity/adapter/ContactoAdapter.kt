//package com.project.projectsegurity.adapter
//
//import android.view.*
//import android.widget.*
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.R
//import com.project.projectsegurity.model.ContactoEmergencia
//
//class ContactoAdapter(
//    private val lista: MutableList<ContactoEmergencia>,
//    private val onDelete: (ContactoEmergencia) -> Unit
//) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
//        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
//        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
//    }
//
//    // 🔥 CORREGIDO: retorna ViewHolder (no View)
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_contacto, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount() = lista.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contacto = lista[position]
//
//        holder.tvNombre.text = contacto.nombre
//        holder.tvTelefono.text = contacto.telefono
//
//        holder.btnEliminar.setOnClickListener {
//            onDelete(contacto)
//            lista.removeAt(position)
//            notifyDataSetChanged()
//        }
//    }
//}

//
//package com.project.projectsegurity.adapter
//
//import android.view.*
//import android.widget.*
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.R
//import com.project.projectsegurity.model.ContactoEmergencia
//
//class ContactoAdapter(
//    private val lista: MutableList<ContactoEmergencia>,
//    private val onDelete: (ContactoEmergencia) -> Unit
//) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
//        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
//        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_contacto, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount() = lista.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contacto = lista[position]
//
//        holder.tvNombre.text = contacto.nombre
//        holder.tvTelefono.text = contacto.telefono
//
//        holder.btnEliminar.setOnClickListener {
//
//            // 🔥 CAMBIO 1: usar adapterPosition (correcto)
//            val pos = holder.adapterPosition
//
//            // 🔥 CAMBIO 2: validar posición válida
//            if (pos != RecyclerView.NO_POSITION) {
//
//                val contactoSeleccionado = lista[pos]
//
//                // 🔥 CAMBIO 3: SOLO delegar al Activity
//                onDelete(contactoSeleccionado)
//
//                // ❌ ELIMINAR ESTO (IMPORTANTE)
//                // lista.removeAt(position)
//                // notifyDataSetChanged()
//            }
//        }
//    }
//}






//package com.project.projectsegurity.adapter
//
//import android.view.*
//import android.widget.*
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.R
//import com.project.projectsegurity.model.ContactoEmergencia
//
//class ContactoAdapter(
//    private val lista: MutableList<ContactoEmergencia>,
//    private val onDelete: (ContactoEmergencia) -> Unit
//) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
//        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
//        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_contacto, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount() = lista.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contacto = lista[position]
//
//        holder.tvNombre.text = contacto.nombre
//        holder.tvTelefono.text = contacto.telefono
//
//        holder.btnEliminar.setOnClickListener {
//
//            val pos = holder.adapterPosition
//
//            if (pos != RecyclerView.NO_POSITION) {
//
//                val contactoSeleccionado = lista[pos]
//
//                // 🔥 SOLO NOTIFICA AL ACTIVITY (NO ELIMINA AQUÍ)
//                onDelete(contactoSeleccionado)
//            }
//        }
//    }
//}



//package com.project.projectsegurity.adapter
//
//import android.view.*
//import android.widget.*
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.R
//import com.project.projectsegurity.model.ContactoEmergencia
//
//class ContactoAdapter(
//    private val lista: MutableList<ContactoEmergencia>,
//    private val onDelete: (ContactoEmergencia) -> Unit
//) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
//        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
//        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_contacto, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount() = lista.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contacto = lista[position]
//
//        holder.tvNombre.text = contacto.nombre
//        holder.tvTelefono.text = contacto.telefono
//
//        holder.btnEliminar.setOnClickListener {
//
//            val pos = holder.adapterPosition
//
//            if (pos != RecyclerView.NO_POSITION) {
//                val contactoSeleccionado = lista[pos]
//
//                // 🔥 CAMBIO CLAVE:
//                // NO eliminar aquí → solo avisar al Activity
//                onDelete(contactoSeleccionado)
//            }
//        }
//    }
//}


//package com.project.projectsegurity.adapter
//
//import android.view.*
//import android.widget.*
//import androidx.recyclerview.widget.RecyclerView
//import com.project.projectsegurity.R
//import com.project.projectsegurity.model.ContactoEmergencia
//
//class ContactoAdapter(
//    private val lista: MutableList<ContactoEmergencia>,
//    private val onDelete: (ContactoEmergencia) -> Unit,
//    private val onClickItem: (ContactoEmergencia) -> Unit // 🔥 CAMBIO 1: NUEVO callback para click en item
//) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
//        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
//        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_contacto, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount() = lista.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contacto = lista[position]
//
//        holder.tvNombre.text = contacto.nombre
//        holder.tvTelefono.text = contacto.telefono
//
//        // 🔥 CAMBIO 2: CLICK EN TODO EL ITEM (para llenar formulario)
//        holder.itemView.setOnClickListener {
//            val pos = holder.adapterPosition
//            if (pos != RecyclerView.NO_POSITION) {
//                onClickItem(lista[pos]) // 🔥 envía contacto al Activity
//            }
//        }
//
//        // 🔥 BOTÓN ELIMINAR (se mantiene igual)
//        holder.btnEliminar.setOnClickListener {
//            val pos = holder.adapterPosition
//            if (pos != RecyclerView.NO_POSITION) {
//                val contactoSeleccionado = lista[pos]
//                onDelete(contactoSeleccionado)
//            }
//        }
//    }
//}







package com.project.projectsegurity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.projectsegurity.R
import com.project.projectsegurity.model.ContactoEmergencia

class ContactoAdapter(
    private val lista: List<ContactoEmergencia>,
    private val onDelete: (ContactoEmergencia) -> Unit,
    private val onEdit: (ContactoEmergencia) -> Unit // 🔥 CAMBIO: antes era onClick
) : RecyclerView.Adapter<ContactoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)

        // =====================================================
        // 🔥 NUEVO BOTÓN EDITAR
        // =====================================================
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacto, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contacto = lista[position]

       // holder.tvNombre.text = contacto.nombre

        holder.tvNombre.text = "${contacto.nombre}  ${contacto.apellido} - ${contacto.relacion}"

        holder.tvTelefono.text = contacto.telefono

        holder.btnEliminar.setOnClickListener {
            onDelete(contacto)
        }

        // =====================================================
        // 🔥 CLICK EN BOTÓN EDITAR
        // 👉 LLAMA A LA ACTIVITY PARA LLENAR INPUTS
        // =====================================================
        holder.btnEditar.setOnClickListener {
            onEdit(contacto)
        }
    }
}