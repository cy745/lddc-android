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

#include "tfile.h"
#include "tbytevectorlist.h"
#include "taglib_export.h"

#ifndef TAGLIB_OGGFILE_H
#define TAGLIB_OGGFILE_H

namespace TagLib {

  //! A namespace for the classes used by Ogg-based metadata files

  namespace Ogg {

    class PageHeader;

    //! An implementation of TagLib::File with some helpers for Ogg based formats

    /*!
     * This is an implementation of Ogg file page and packet rendering and is of
     * use to Ogg based formats.  While the API is small this handles the
     * non-trivial details of breaking up an Ogg stream into packets and makes
     * these available (via subclassing) to the codec meta data implementations.
     */

    class TAGLIB_EXPORT File : public TagLib::File
    {
    public:
      ~File() override;

      File(const File &) = delete;
      File &operator=(const File &) = delete;

      /*!
       * Returns the packet contents for the i-th packet (starting from zero)
       * in the Ogg bitstream.
       *
       * \warning This requires reading at least the packet header for every page
       * up to the requested page.
       */
      ByteVector packet(unsigned int i);

      /*!
       * Sets the packet with index \a i to the value \a p.
       */
      void setPacket(unsigned int i, const ByteVector &p);

      /*!
       * Returns a pointer to the PageHeader for the first page in the stream or
       * null if the page could not be found.
       */
      const PageHeader *firstPageHeader();

      /*!
       * Returns a pointer to the PageHeader for the last page in the stream or
       * null if the page could not be found.
       */
      const PageHeader *lastPageHeader();

      bool save() override;

    protected:
      /*!
       * Constructs an Ogg file from \a file.
       *
       * \note This constructor is protected since Ogg::File shouldn't be
       * instantiated directly but rather should be used through the codec
       * specific subclasses.
       */
      File(FileName file);

      /*!
       * Constructs an Ogg file from \a stream.
       *
       * \note This constructor is protected since Ogg::File shouldn't be
       * instantiated directly but rather should be used through the codec
       * specific subclasses.
       *
       * \note TagLib will *not* take ownership of the stream, the caller is
       * responsible for deleting it after the File object.
       */
      File(IOStream *stream);

    private:
      /*!
       * Reads the pages from the beginning of the file until enough to compose
       * the requested packet.
       */
      bool readPages(unsigned int i);

      /*!
       * Writes the requested packet to the file.
       */
      void writePacket(unsigned int i, const ByteVector &packet);

      class FilePrivate;
      TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
      std::unique_ptr<FilePrivate> d;
    };

  }  // namespace Ogg
}  // namespace TagLib

#endif
