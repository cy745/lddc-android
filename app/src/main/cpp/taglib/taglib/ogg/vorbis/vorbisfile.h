/***************************************************************************
    copyright            : (C) 2002 - 2008 by Scott Wheeler
    email                : wheeler@kde.org
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License version   *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA         *
 *   02110-1301  USA                                                       *
 *                                                                         *
 *   Alternatively, this file is available under the Mozilla Public        *
 *   License Version 1.1.  You may obtain a copy of the License at         *
 *   http://www.mozilla.org/MPL/                                           *
 ***************************************************************************/

#ifndef TAGLIB_VORBISFILE_H
#define TAGLIB_VORBISFILE_H

#include "taglib_export.h"
#include "oggfile.h"
#include "xiphcomment.h"
#include "vorbisproperties.h"

namespace TagLib {

/*
 * This is just to make this appear to be in the Ogg namespace in the
 * documentation.  The typedef below will make this work with the current code.
 * In the next BIC version of TagLib this will be really moved into the Ogg
 * namespace.
 * Kept for source compatibility, the typedef in vorbisproperties.h was not
 * correct in TagLib 1.
 */

#ifdef DOXYGEN
  namespace Ogg {
#endif

  //! A namespace containing classes for Vorbis metadata

  namespace Vorbis {


    //! An implementation of Ogg::File with Vorbis specific methods

    /*!
     * This is the central class in the Ogg Vorbis metadata processing collection
     * of classes.  It's built upon Ogg::File which handles processing of the Ogg
     * logical bitstream and breaking it down into pages which are handled by
     * the codec implementations, in this case Vorbis specifically.
     */

    class TAGLIB_EXPORT File : public Ogg::File
    {
    public:
      /*!
       * Constructs a Vorbis file from \a file.  If \a readProperties is true the
       * file's audio properties will also be read.
       *
       * \note In the current implementation, \a propertiesStyle is ignored.
       */
      File(FileName file, bool readProperties = true,
           Properties::ReadStyle propertiesStyle = Properties::Average);

      /*!
       * Constructs a Vorbis file from \a stream.  If \a readProperties is true the
       * file's audio properties will also be read.
       *
       * \note TagLib will *not* take ownership of the stream, the caller is
       * responsible for deleting it after the File object.
       *
       * \note In the current implementation, \a propertiesStyle is ignored.
       */
      File(IOStream *stream, bool readProperties = true,
           Properties::ReadStyle propertiesStyle = Properties::Average);

      /*!
       * Destroys this instance of the File.
       */
      ~File() override;

      File(const File &) = delete;
      File &operator=(const File &) = delete;

      /*!
       * Returns the XiphComment for this file.  XiphComment implements the tag
       * interface, so this serves as the reimplementation of
       * TagLib::File::tag().
       */
      Ogg::XiphComment *tag() const override;


      /*!
       * Implements the unified property interface -- export function.
       * This forwards directly to XiphComment::properties().
       */
      PropertyMap properties() const override;

      /*!
       * Implements the unified tag dictionary interface -- import function.
       * Like properties(), this is a forwarder to the file's XiphComment.
       */
      PropertyMap setProperties(const PropertyMap &) override;

      /*!
       * Returns the Vorbis::Properties for this file.  If no audio properties
       * were read then this will return a null pointer.
       */
      Properties *audioProperties() const override;

      /*!
       * Save the file.
       *
       * This returns true if the save was successful.
       */
      bool save() override;

      /*!
       * Check if the given \a stream can be opened as an Ogg Vorbis file.
       *
       * \note This method is designed to do a quick check.  The result may
       * not necessarily be correct.
       */
      static bool isSupported(IOStream *stream);

    private:
      void read(bool readProperties);

      class FilePrivate;
      TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
      std::unique_ptr<FilePrivate> d;
    };
  }  // namespace Vorbis

/*
 * To keep compatibility with the current version put Vorbis in the Ogg namespace
 * only in the docs and provide a typedef to make it work.  In the next BIC
 * version this will be removed and it will only exist in the Ogg namespace.
 * Kept for source compatibility, the typedef in vorbisproperties.h was not
 * correct in TagLib 1.
 */

#ifdef DOXYGEN
  }
#else
  namespace Ogg {
    namespace Vorbis {
      using File = TagLib::Vorbis::File;
    }
  }
#endif

}  // namespace TagLib

#endif
